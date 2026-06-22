package jp.kcgi.bousai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AI 問答のリクエスト DTO。
 *
 * @param question 質問文（必須）
 * @param lang     回答言語（任意。未指定なら ja 扱い）
 */
public record ChatRequest(
        @NotBlank
        @Size(max = 1000)
        String question,

        @Size(max = 16)
        String lang
) {
}
