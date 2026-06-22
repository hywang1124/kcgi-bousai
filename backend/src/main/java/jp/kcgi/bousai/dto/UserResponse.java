package jp.kcgi.bousai.dto;

import jp.kcgi.bousai.domain.Role;
import jp.kcgi.bousai.domain.User;

/**
 * ユーザのレスポンス DTO（パスワードハッシュは含めない）。
 */
public record UserResponse(
        Long id,
        String username,
        Role role,
        boolean enabled
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.isEnabled());
    }
}
