package jp.kcgi.bousai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 関連の設定。フロントエンド（Vite 開発サーバ等）からの CORS を許可する。
 *
 * <p>許可オリジンは {@code app.cors.allowed-origins} で上書き可能。
 * 既定は Vite(5173) と CRA(3000) のローカル開発ポート。</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebConfig(
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
            String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
