package jp.kcgi.bousai.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

/**
 * Spring AI（{@code ChatClient}）+ OpenAI + RAG による {@link ChatAssistant} 実装。
 *
 * <p>RAG は {@code RetrievalAugmentationAdvisor} + {@code VectorStoreDocumentRetriever} +
 * {@code VectorStore} で構成し、{@code classpath:rag-corpus/} の防災資料（{@code RagCorpusLoader}
 * が起動時に投入）を検索して回答に注入する。</p>
 *
 * <p><b>{@code allowEmptyContext(true)} が必須</b>：既定の {@code ContextualQueryAugmenter}
 * は類似度がしきい値に満たず資料が見つからない場合、ユーザーの質問文そのものを破棄して
 * 「The user query is outside your knowledge base.」という英語の固定文に置き換えてしまう
 * （Spring AI の既定挙動）。これでは元の質問が LLM に届かず、安全要件（OPENAI.md §6 / §11）
 * で求める「資料にありません」という案内（質問内容に応じた多言語応答）も機能しない。
 * {@code allowEmptyContext(true)} にすることで資料が無い場合は質問文をそのまま渡し、
 * system prompt の指示（資料が無ければ「資料にありません」と明示する）に委ねる。</p>
 */
public class SpringAiChatAssistant implements ChatAssistant {

    private static final Logger openAiRequestLog = LoggerFactory.getLogger("jp.kcgi.bousai.openai");

    private static final String SYSTEM_TEMPLATE = """
            あなたは防災情報アシスタントです。次の方針を厳守してください。
            - 提供された資料（コンテキスト）に基づいてのみ回答する。
            - 資料に該当情報が無い場合は「資料にありません」と明示し、お住まいの自治体の
              公式情報を確認するよう案内する。
            - 避難所の位置・電話番号・災害指示を絶対に創作しない（安全上重要）。
            - 必ず利用者の言語（言語コード: %s）で回答する。
            """;

    private final ChatClient chatClient;

    public SpringAiChatAssistant(OpenAiChatModel chatModel, VectorStore vectorStore) {
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
        String requestId = UUID.randomUUID().toString();
        long startedAt = System.currentTimeMillis();
        logOpenAiRequestStart(requestId, "chat", question, lang);
        try {
            String text = chatClient.prompt()
                    .system(SYSTEM_TEMPLATE.formatted(lang))
                    .user(question)
                    .call()
                    .content();
            logOpenAiRequestEnd(requestId, "chat", startedAt, text);
            return new ChatAnswer(text, List.of());
        } catch (RuntimeException error) {
            logOpenAiRequestError(requestId, "chat", startedAt, error);
            throw error;
        }
    }

    @Override
    public Flux<String> generateStream(String question, String lang) {
        String requestId = UUID.randomUUID().toString();
        long startedAt = System.currentTimeMillis();
        StringBuilder answer = new StringBuilder();
        logOpenAiRequestStart(requestId, "stream", question, lang);
        return chatClient.prompt()
                .system(SYSTEM_TEMPLATE.formatted(lang))
                .user(question)
                .stream()
                .content()
                .doOnNext(answer::append)
                .doOnComplete(() -> logOpenAiRequestEnd(requestId, "stream", startedAt, answer.toString()))
                .doOnError(error -> logOpenAiRequestError(requestId, "stream", startedAt, error));
    }

    private static void logOpenAiRequestStart(String requestId, String mode, String question, String lang) {
        openAiRequestLog.info(
                "openai_request_start requestId={} mode={} lang={} questionLength={} question=\"{}\"",
                requestId,
                mode,
                lang,
                question.length(),
                escapeLogValue(question));
    }

    private static void logOpenAiRequestEnd(String requestId, String mode, long startedAt, String answer) {
        openAiRequestLog.info(
                "openai_request_end requestId={} mode={} durationMs={} answerLength={} answer=\"{}\"",
                requestId,
                mode,
                System.currentTimeMillis() - startedAt,
                answer.length(),
                escapeLogValue(answer));
    }

    private static void logOpenAiRequestError(String requestId, String mode, long startedAt, Throwable error) {
        openAiRequestLog.warn(
                "openai_request_error requestId={} mode={} durationMs={} errorType={} message=\"{}\"",
                requestId,
                mode,
                System.currentTimeMillis() - startedAt,
                error.getClass().getSimpleName(),
                escapeLogValue(error.getMessage() == null ? "" : error.getMessage()));
    }

    private static String escapeLogValue(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\"", "\\\"");
    }
}
