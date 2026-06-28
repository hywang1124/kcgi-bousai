package jp.kcgi.bousai.ai;

import java.util.List;

/**
 * AI アシスタントの回答結果。
 *
 * @param text    回答本文
 * @param sources 参照元（現在は未使用）
 */
public record ChatAnswer(String text, List<String> sources) {
}
