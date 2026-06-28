package jp.kcgi.bousai.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestionLanguageDetectorTest {

    private final QuestionLanguageDetector detector = new QuestionLanguageDetector();

    @Test
    void detectsChineseQuestionEvenWhenUiLanguageIsJapanese() {
        assertEquals("zh", detector.resolveAnswerLang("请介绍一下地震", "ja"));
    }

    @Test
    void detectsJapaneseQuestion() {
        assertEquals("ja", detector.resolveAnswerLang("地震について教えてください", "zh"));
    }

    @Test
    void fallsBackToRequestedLanguageWhenQuestionIsAmbiguous() {
        assertEquals("en", detector.resolveAnswerLang("12345", "en"));
    }
}
