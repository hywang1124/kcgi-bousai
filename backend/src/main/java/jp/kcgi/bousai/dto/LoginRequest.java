package jp.kcgi.bousai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * ログインリクエスト DTO。
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
