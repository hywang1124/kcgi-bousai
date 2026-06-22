package jp.kcgi.bousai.ai;

/**
 * 防災 AI 問答のポート（抽象）。
 *
 * <p>現在は {@link MockChatAssistant} による仮実装。将来 Spring AI の
 * {@code ChatClient} + RAG（{@code RetrievalAugmentationAdvisor}）を用いた
 * 実装に差し替える前提で、呼び出し側はこのインタフェースにのみ依存する。</p>
 */
public interface ChatAssistant {

    /**
     * 質問に回答する。
     *
     * @param question 質問文
     * @param lang     回答言語（ja / en / zh）
     * @return 回答本文と参照元
     */
    ChatAnswer generate(String question, String lang);
}
