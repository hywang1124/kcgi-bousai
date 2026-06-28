package jp.kcgi.bousai.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

/**
 * Spring AI（{@code ChatClient}）+ OpenAI による {@link ChatAssistant} 実装。
 *
 * <p>RAG は使わず、一般的な防災知識に範囲を限定して回答する。避難所の位置・電話番号・開設状況、
 * 現在の警報、地域別の最新指示などは、自治体や気象庁などの公式情報の確認を案内する。</p>
 */
public class SpringAiChatAssistant implements ChatAssistant {

    private static final Logger openAiRequestLog = LoggerFactory.getLogger("jp.kcgi.bousai.openai");

    private static final String SYSTEM_TEMPLATE = """
            あなたは防災情報アシスタントです。次の方針を厳守してください。
            - 地震、津波、台風、大雨、洪水、土砂災害、火山、備蓄、避難行動などの一般的な防災知識だけを回答する。
            - 避難所の位置・電話番号・開設状況、避難ルート、現在の警報、現在の災害状況、地域別の最新指示は回答しない。
            - 最新情報や地域別情報を聞かれた場合は、このシステムでは確認できないことを明示し、自治体・気象庁・国土地理院などの公式最新情報を確認するよう案内する。
            - 分からない内容や根拠が不確かな内容は推測せず、「このシステムでは確認できません」と明示する。
            - 避難所情報、電話番号、災害指示を絶対に創作しない（安全上重要）。
            - 必ず利用者の言語（言語コード: %s）で、子どもや外国人にも分かりやすい表現で回答する。
            """;

    private final ChatClient chatClient;

    public SpringAiChatAssistant(OpenAiChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public ChatAnswer generate(String question, String lang) {
        String requestId = UUID.randomUUID().toString();
        long startedAt = System.currentTimeMillis();
        String systemPrompt = SYSTEM_TEMPLATE.formatted(lang);
        logOpenAiRequestStart(requestId, "chat", question, lang);
        logOpenAiPrompt(requestId, "chat", lang, systemPrompt, question);
        try {
            String text = chatClient.prompt()
                    .system(systemPrompt)
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
        String systemPrompt = SYSTEM_TEMPLATE.formatted(lang);
        StringBuilder answer = new StringBuilder();
        logOpenAiRequestStart(requestId, "stream", question, lang);
        logOpenAiPrompt(requestId, "stream", lang, systemPrompt, question);
        return chatClient.prompt()
                .system(systemPrompt)
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

    private static void logOpenAiPrompt(String requestId, String mode, String lang, String systemPrompt, String question) {
        logOpenAiPromptMessage(requestId, mode, lang, 0, "system", systemPrompt);
        logOpenAiPromptMessage(requestId, mode, lang, 1, "user", question);
    }

    private static void logOpenAiPromptMessage(
            String requestId, String mode, String lang, int index, String role, String content) {
        openAiRequestLog.info(
                "openai_prompt requestId={} mode={} lang={} index={} role={} contentLength={} content=\"{}\"",
                requestId,
                mode,
                lang,
                index,
                role,
                content.length(),
                escapeLogValue(content));
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
