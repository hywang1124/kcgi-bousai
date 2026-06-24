package jp.kcgi.bousai.ai;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Spring AI（{@code ChatClient}）+ Claude（Anthropic）+ RAG による {@link ChatAssistant} 実装。
 *
 * <p>RAG は {@code RetrievalAugmentationAdvisor} と {@code VectorStore} で構成するが、
 * 現時点では防災資料の語料投入は行っていない（{@code AiAssistantConfig} 参照）。
 * 資料が見つからない場合は安全要件（CLAUDE.md §6 / §11）に従い、自治体への確認を
 * 案内するよう system prompt で指示する。</p>
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
        Advisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(0.5)
                        .topK(4)
                        .build())
                .build();
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(ragAdvisor)
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
