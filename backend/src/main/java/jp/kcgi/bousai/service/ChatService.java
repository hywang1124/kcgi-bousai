package jp.kcgi.bousai.service;

import jp.kcgi.bousai.ai.ChatAnswer;
import jp.kcgi.bousai.ai.ChatAssistant;
import jp.kcgi.bousai.ai.QuestionLanguageDetector;
import jp.kcgi.bousai.dto.ChatRequest;
import jp.kcgi.bousai.dto.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * AI 問答のビジネスロジック。回答を生成して返す（永続化は行わない＝DB なし）。
 */
@Service
public class ChatService {

    private final ChatAssistant chatAssistant;
    private final QuestionLanguageDetector questionLanguageDetector;

    public ChatService(ChatAssistant chatAssistant, QuestionLanguageDetector questionLanguageDetector) {
        this.chatAssistant = chatAssistant;
        this.questionLanguageDetector = questionLanguageDetector;
    }

    /**
     * 質問に回答する（非ストリーミング）。
     */
    public ChatResponse ask(ChatRequest request) {
        String lang = questionLanguageDetector.resolveAnswerLang(request.question(), request.lang());
        ChatAnswer answer = chatAssistant.generate(request.question(), lang);
        return new ChatResponse(answer.text(), lang, answer.sources());
    }

    /**
     * 質問に回答する（ストリーミング）。テキスト断片を逐次発行する。
     */
    public Flux<String> streamAsk(ChatRequest request) {
        String lang = questionLanguageDetector.resolveAnswerLang(request.question(), request.lang());
        return chatAssistant.generateStream(request.question(), lang);
    }
}
