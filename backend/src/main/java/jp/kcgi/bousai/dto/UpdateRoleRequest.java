package jp.kcgi.bousai.dto;

import jakarta.validation.constraints.NotNull;
import jp.kcgi.bousai.domain.Role;

/**
 * 役割変更リクエスト DTO（管理者用）。
 *
 * @param role 新しい役割（ADMIN / EDITOR / USER）
 */
public record UpdateRoleRequest(
        @NotNull Role role
) {
}
