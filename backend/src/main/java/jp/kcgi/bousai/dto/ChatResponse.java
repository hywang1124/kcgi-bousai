package jp.kcgi.bousai.dto;

import java.util.List;

/**
 * AI 問答のレスポンス DTO。
 *
 * @param answer  回答本文
 * @param lang    回答言語
 * @param sources 参照元一覧
 */
public record ChatResponse(
        String answer,
        String lang,
        List<String> sources
) {
}
