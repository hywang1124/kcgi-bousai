package jp.kcgi.bousai.controller;

import jakarta.validation.Valid;
import jp.kcgi.bousai.dto.ChatRequest;
import jp.kcgi.bousai.dto.ChatResponse;
import jp.kcgi.bousai.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 防災問答 REST API。
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 質問に回答する（非ストリーミング）。
     */
    @PostMapping
    public ChatResponse ask(@Valid @RequestBody ChatRequest request) {
        return chatService.ask(request);
    }

    /**
     * 質問に回答する（ストリーミング、SSE）。
     *
     * <p>テキスト断片を {@code Flux<String>} として返す。SSE のフレーム化
     * （{@code data: ...\n\n}）は Spring MVC が {@code produces} に応じて自動で行う。</p>
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@Valid @RequestBody ChatRequest request) {
        return chatService.streamAsk(request);
    }
}
