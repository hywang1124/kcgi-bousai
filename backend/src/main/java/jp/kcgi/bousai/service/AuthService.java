package jp.kcgi.bousai.service;

import jp.kcgi.bousai.dto.LoginRequest;
import jp.kcgi.bousai.dto.LoginResponse;
import jp.kcgi.bousai.security.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * 認証ロジック。資格情報を検証し、JWT を発行する。
 */
@Service
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthService(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    /**
     * ログインを処理する。資格情報が不正な場合は {@link org.springframework.security.core.AuthenticationException}。
     */
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String token = tokenService.generate(authentication);
        return new LoginResponse(token, TOKEN_TYPE, tokenService.ttlSeconds());
    }
}
