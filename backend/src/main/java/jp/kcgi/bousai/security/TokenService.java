package jp.kcgi.bousai.security;

import jp.kcgi.bousai.config.JwtProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 認証済みユーザに対する JWT を発行する。
 */
@Service
public class TokenService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public TokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 認証情報から JWT を生成する。{@code roles} クレームに ROLE_ プレフィックスを除いた役割を格納する。
     */
    public String generate(Authentication authentication) {
        Instant now = Instant.now();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(ROLE_PREFIX))
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("bousai")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtProperties.ttlSeconds()))
                .subject(authentication.getName())
                .claim("roles", roles)
                .build();

        // 対称鍵のため JWS ヘッダのアルゴリズムを HS256 に明示する
        // （未指定だと既定が RS256 となり鍵選択に失敗する）
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long ttlSeconds() {
        return jwtProperties.ttlSeconds();
    }
}
