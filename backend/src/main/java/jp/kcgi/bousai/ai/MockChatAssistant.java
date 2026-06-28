package jp.kcgi.bousai.ai;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

/**
 * {@link ChatAssistant} の仮実装。LLM 未接続（API キー未設定）でもリンクを通すためのモック。
 *
 * <p>安全要件（OPENAI.md §6 / §11）に従い、避難所の位置・電話・災害指示を一切
 * 創作しない。言語に応じた定型の案内文を返し、詳細は自治体の公式情報へ誘導する。</p>
 *
 * <p>{@code generateStream} は本物の LLM ストリーミング応答を模すため、回答を
 * 単語単位に分割し一定間隔で逐次発行する。</p>
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
                            + "For evacuation shelters and disaster guidance, please refer to the official "
                            + "information published by your local municipality.",
                    List.of("Disaster Prevention Guide (sample)"));
            case "zh" -> new ChatAnswer(
                    "【模拟回答】感谢提问。AI 助手尚未接入。有关避难所与灾害应对，"
                            + "请以您所在地自治体的官方信息为准。",
                    List.of("防灾指南（样例）"));
            case "zh-tw" -> new ChatAnswer(
                    "【模擬回答】感謝提問。AI 助手尚未接入。有關避難所與災害應對，"
                            + "請以您所在地自治體的官方資訊為準。",
                    List.of("防災指南（樣例）"));
            default -> new ChatAnswer(
                    "【仮回答】ご質問ありがとうございます。現在 AI は準備中です。"
                            + "避難所や災害時の対応については、お住まいの自治体の公式情報をご確認ください。",
                    List.of("防災ガイド（サンプル）"));
        };
    }
}
