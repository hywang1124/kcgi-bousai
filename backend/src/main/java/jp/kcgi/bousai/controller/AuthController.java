package jp.kcgi.bousai.controller;

import jakarta.validation.Valid;
import jp.kcgi.bousai.dto.LoginRequest;
import jp.kcgi.bousai.dto.LoginResponse;
import jp.kcgi.bousai.dto.RegisterRequest;
import jp.kcgi.bousai.dto.UserResponse;
import jp.kcgi.bousai.service.AuthService;
import jp.kcgi.bousai.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 認証 API。公開のユーザ登録とログイン（JWT 取得）。
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /** セルフ登録（公開・既定ロール USER）。 */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
