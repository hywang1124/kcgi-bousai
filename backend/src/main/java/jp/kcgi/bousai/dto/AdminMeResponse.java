package jp.kcgi.bousai.dto;

import java.util.List;

/**
 * 認証中の管理者情報。
 *
 * @param username ユーザ名
 * @param roles    役割一覧（ROLE_ プレフィックス除去済み）
 */
public record AdminMeResponse(
        String username,
        List<String> roles
) {
}
