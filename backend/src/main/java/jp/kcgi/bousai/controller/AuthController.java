package jp.kcgi.bousai.controller;

import jakarta.validation.Valid;
import jp.kcgi.bousai.dto.LoginRequest;
import jp.kcgi.bousai.dto.LoginResponse;
import jp.kcgi.bousai.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 認証 API。ログインして JWT を取得する。
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
