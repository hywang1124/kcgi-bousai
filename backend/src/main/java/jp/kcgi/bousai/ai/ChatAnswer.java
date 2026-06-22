package jp.kcgi.bousai.ai;

import java.util.List;

/**
 * AI アシスタントの回答結果。
 *
 * @param text    回答本文
 * @param sources 参照元（RAG 導入後は検索ヒット文書のタイトル等）
 */
public record ChatAnswer(String text, List<String> sources) {
}
