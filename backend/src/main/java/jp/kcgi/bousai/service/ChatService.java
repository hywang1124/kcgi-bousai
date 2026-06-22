package jp.kcgi.bousai.service;

import jp.kcgi.bousai.ai.ChatAnswer;
import jp.kcgi.bousai.ai.ChatAssistant;
import jp.kcgi.bousai.domain.ChatLog;
import jp.kcgi.bousai.dto.ChatRequest;
import jp.kcgi.bousai.dto.ChatResponse;
import jp.kcgi.bousai.repository.ChatLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 問答のビジネスロジック。回答生成とログ保存を編排する。
 */
@Service
public class ChatService {

    private static final String DEFAULT_LANG = "ja";

    private final ChatAssistant chatAssistant;
    private final ChatLogRepository chatLogRepository;

    public ChatService(ChatAssistant chatAssistant, ChatLogRepository chatLogRepository) {
        this.chatAssistant = chatAssistant;
        this.chatLogRepository = chatLogRepository;
    }

    /**
     * 質問に回答し、問答ログを保存する。
     */
    @Transactional
    public ChatResponse ask(ChatRequest request) {
        String lang = (request.lang() == null || request.lang().isBlank())
                ? DEFAULT_LANG
                : request.lang();

        ChatAnswer answer = chatAssistant.generate(request.question(), lang);

        chatLogRepository.save(new ChatLog(
                request.question(),
                answer.text(),
                lang,
                String.join(", ", answer.sources())
        ));

        return new ChatResponse(answer.text(), lang, answer.sources());
    }
}
