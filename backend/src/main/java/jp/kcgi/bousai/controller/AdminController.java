package jp.kcgi.bousai.controller;

import jp.kcgi.bousai.dto.AdminMeResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理後台 API（ADMIN ロール必須。アクセス制御は {@code SecurityConfig} で定義）。
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * 認証中の管理者情報を返す（保護の動作確認用）。
     */
    @GetMapping("/me")
    public AdminMeResponse me(Authentication authentication) {
        // ROLE_ 権限のみを役割として返す（Spring Security 7 が付与する FACTOR_BEARER 等は除外）
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(ROLE_PREFIX))
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .toList();
        return new AdminMeResponse(authentication.getName(), roles);
    }
}
