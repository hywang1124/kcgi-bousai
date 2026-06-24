package jp.kcgi.bousai.ai;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Spring AI（{@code ChatClient}）+ Claude（Anthropic）+ RAG による {@link ChatAssistant} 実装。
 *
 * <p>RAG は {@code RetrievalAugmentationAdvisor} + {@code VectorStoreDocumentRetriever} +
 * {@code VectorStore} で構成し、{@code classpath:rag-corpus/} の防災資料（{@code RagCorpusLoader}
 * が起動時に投入）を検索して回答に注入する。</p>
 *
 * <p><b>{@code allowEmptyContext(true)} が必須</b>：既定の {@code ContextualQueryAugmenter}
 * は類似度がしきい値に満たず資料が見つからない場合、ユーザーの質問文そのものを破棄して
 * 「The user query is outside your knowledge base.」という英語の固定文に置き換えてしまう
 * （Spring AI の既定挙動）。これでは元の質問が LLM に届かず、安全要件（CLAUDE.md §6 / §11）
 * で求める「資料にありません」という案内（質問内容に応じた多言語応答）も機能しない。
 * {@code allowEmptyContext(true)} にすることで資料が無い場合は質問文をそのまま渡し、
 * system prompt の指示（資料が無ければ「資料にありません」と明示する）に委ねる。</p>
 */
public class SpringAiChatAssistant implements ChatAssistant {

    private static final String SYSTEM_TEMPLATE = """
            あなたは防災情報アシスタントです。次の方針を厳守してください。
            - 提供された資料（コンテキスト）に基づいてのみ回答する。
            - 資料に該当情報が無い場合は「資料にありません」と明示し、お住まいの自治体の
              公式情報を確認するよう案内する。
            - 避難所の位置・電話番号・災害指示を絶対に創作しない（安全上重要）。
            - 必ず利用者の言語（言語コード: %s）で回答する。
            """;

    private final ChatClient chatClient;

    public SpringAiChatAssistant(AnthropicChatModel chatModel, VectorStore vectorStore) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(buildRagAdvisor(vectorStore))
                .build();
    }

    /**
     * RAG Advisor を構築する（テストから直接呼べるよう package-private に分離）。
     */
    static Advisor buildRagAdvisor(VectorStore vectorStore) {
        QueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(0.5)
                        .topK(4)
                        .build())
                .queryAugmenter(queryAugmenter)
                .build();
    }

    @Override
    public ChatAnswer generate(String question, String lang) {
        String text = chatClient.prompt()
                .system(SYSTEM_TEMPLATE.formatted(lang))
                .user(question)
                .call()
                .content();
        return new ChatAnswer(text, List.of());
    }

    @Override
    public Flux<String> generateStream(String question, String lang) {
        return chatClient.prompt()
                .system(SYSTEM_TEMPLATE.formatted(lang))
                .user(question)
                .stream()
                .content();
    }
}
