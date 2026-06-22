package jp.kcgi.bousai.dto;

/**
 * ログインレスポンス DTO。
 *
 * @param token     JWT アクセストークン
 * @param tokenType トークン種別（Bearer）
 * @param expiresIn 有効期間（秒）
 */
public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn
) {
}
