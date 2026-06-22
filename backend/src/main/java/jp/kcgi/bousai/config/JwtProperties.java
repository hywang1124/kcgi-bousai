package jp.kcgi.bousai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * JWT 設定。{@code app.jwt.*} にバインドする。
 *
 * @param secret     対称鍵(HS256)。32 バイト以上必須。機密のため環境変数で渡す。
 * @param ttlSeconds トークン有効期間（秒）
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        @DefaultValue("3600") long ttlSeconds
) {
}
