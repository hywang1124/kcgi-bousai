package jp.kcgi.bousai.ai;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

/**
 * {@link ChatAssistant} の仮実装。LLM 未接続（API キー未設定）でもリンクを通すためのモック。
 *
 * <p>避難所の位置・電話番号・災害指示を創作せず、自治体などの公式最新情報の確認を案内する。</p>
 */
public class MockChatAssistant implements ChatAssistant {

    @Override
    public ChatAnswer generate(String question, String lang) {
        return answerFor(lang);
    }

    @Override
    public Flux<String> generateStream(String question, String lang) {
        String text = answerFor(lang).text();
        String[] chunks = text.split("(?<=\\s)|(?<=。)|(?<=、)");
        return Flux.fromArray(chunks).delayElements(Duration.ofMillis(60));
    }

    private ChatAnswer answerFor(String lang) {
        String normalized = (lang == null || lang.isBlank()) ? "ja" : lang.toLowerCase();
        return switch (normalized) {
            case "en" -> new ChatAnswer(
                    "[Mock answer] Thank you for your question. The AI assistant is not connected yet. "
                            + "For shelters, warnings, routes, and other latest local information, "
                            + "please check official information from your local municipality or weather agency.",
                    List.of());
            case "zh" -> new ChatAnswer(
                    "【模拟回答】感谢提问。AI 助手尚未连接。关于避难所、警报、路线等最新本地信息，"
                            + "请确认当地自治体或气象机构发布的官方信息。",
                    List.of());
            case "zh-tw" -> new ChatAnswer(
                    "【模擬回答】感謝提問。AI 助手尚未連線。關於避難所、警報、路線等最新在地資訊，"
                            + "請確認當地自治體或氣象機構發布的官方資訊。",
                    List.of());
            default -> new ChatAnswer(
                    "【仮回答】ご質問ありがとうございます。現在 AI は準備中です。避難所、警報、経路などの最新地域情報は、"
                            + "お住まいの自治体や気象機関の公式情報を確認してください。",
                    List.of());
        };
    }
}
