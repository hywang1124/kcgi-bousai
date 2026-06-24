package jp.kcgi.bousai.ai;

import reactor.core.publisher.Flux;

/**
 * 防災 AI 問答のポート（抽象）。
 *
 * <p>API キー未設定時は {@link MockChatAssistant}、設定時は Spring AI の
 * {@code ChatClient} + RAG（{@code RetrievalAugmentationAdvisor}）を用いた
 * 実装に切り替わる（{@code AiAssistantConfig} 参照）。呼び出し側はこのインタフェース
 * にのみ依存する。</p>
 */
public interface ChatAssistant {

    /**
     * 質問に回答する（非ストリーミング）。
     *
     * @param question 質問文
     * @param lang     回答言語（ja / en / zh / zh-TW）
     * @return 回答本文と参照元
     */
    ChatAnswer generate(String question, String lang);

    /**
     * 質問に回答する（ストリーミング）。テキスト断片を逐次発行する。
     *
     * @param question 質問文
     * @param lang     回答言語（ja / en / zh / zh-TW）
     * @return 回答本文の断片を逐次発行する Flux
     */
    Flux<String> generateStream(String question, String lang);
}
