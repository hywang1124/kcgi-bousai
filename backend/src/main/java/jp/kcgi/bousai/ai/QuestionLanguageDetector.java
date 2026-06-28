package jp.kcgi.bousai.ai;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.springframework.stereotype.Component;

/**
 * ユーザー質問文から回答言語を判定する。
 *
 * <p>フロントエンドの表示言語ではなく、質問文そのものの言語を優先する。判定できない短文や
 * 記号だけの入力では、画面で選択された言語にフォールバックする。</p>
 */
@Component
public class QuestionLanguageDetector {

    private static final String DEFAULT_LANG = "ja";

    private final LanguageDetector detector = LanguageDetectorBuilder
            .fromLanguages(Language.JAPANESE, Language.ENGLISH, Language.CHINESE)
            .withMinimumRelativeDistance(0.10)
            .build();

    public String resolveAnswerLang(String question, String requestedLang) {
        String fallback = normalizeLang(requestedLang);
        if (question == null || question.isBlank()) {
            return fallback;
        }

        Language detected = detector.detectLanguageOf(question);
        return switch (detected) {
            case JAPANESE -> "ja";
            case ENGLISH -> "en";
            case CHINESE -> fallback.equals("zh-TW") ? "zh-TW" : "zh";
            default -> fallback;
        };
    }

    private String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) {
            return DEFAULT_LANG;
        }
        return switch (lang) {
            case "en", "zh", "zh-TW" -> lang;
            default -> DEFAULT_LANG;
        };
    }
}
