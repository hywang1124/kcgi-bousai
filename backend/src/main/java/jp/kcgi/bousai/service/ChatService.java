package jp.kcgi.bousai.service;

import jp.kcgi.bousai.ai.ChatAnswer;
import jp.kcgi.bousai.ai.ChatAssistant;
import jp.kcgi.bousai.dto.ChatRequest;
import jp.kcgi.bousai.dto.ChatResponse;
import org.springframework.stereotype.Service;

/**
 * AI 問答のビジネスロジック。回答を生成して返す（永続化は行わない＝DB なし）。
 */
@Service
public class ChatService {

    private static final String DEFAULT_LANG = "ja";

    private final ChatAssistant chatAssistant;

    public ChatService(ChatAssistant chatAssistant) {
        this.chatAssistant = chatAssistant;
    }

    /**
     * 質問に回答する。
     */
    public ChatResponse ask(ChatRequest request) {
        String lang = (request.lang() == null || request.lang().isBlank())
                ? DEFAULT_LANG
                : request.lang();

        ChatAnswer answer = chatAssistant.generate(request.question(), lang);
        return new ChatResponse(answer.text(), lang, answer.sources());
    }
}
