package jp.kcgi.bousai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Auditing の有効化。{@code @CreatedBy/@LastModifiedBy} 用の監査者を提供する。
 *
 * <p>監査者は現在の認証ユーザ名。未認証（公開 API・起動時シード等）の場合は {@code system}。</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    private static final String SYSTEM = "system";

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                return Optional.of(SYSTEM);
            }
            return Optional.of(authentication.getName());
        };
    }
}
