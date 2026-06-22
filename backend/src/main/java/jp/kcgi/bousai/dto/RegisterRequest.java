package jp.kcgi.bousai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ユーザ登録リクエスト DTO（公開セルフ登録）。
 *
 * @param username ログイン ID（3〜64 文字）
 * @param password パスワード（8〜72 文字。72 は BCrypt の上限）
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Size(min = 8, max = 72) String password
) {
}
