package jp.kcgi.bousai.config;

import jp.kcgi.bousai.domain.User;
import jp.kcgi.bousai.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * dev プロファイル限定の管理者シーダ。起動時に管理者が居なければ作成する。
 *
 * <p>本番ではこの初期化は動かない（{@code @Profile("dev")}）。管理者は安全な手順で別途作成する。
 * パスワードはスキーマ管理外の「データ」であり、Flyway ではなくここで投入する。</p>
 */
@Component
@Profile("dev")
public class DevAdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevAdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public DevAdminInitializer(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${app.admin.default-username:admin}") String username,
                               @Value("${app.admin.default-password:admin12345}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        userRepository.save(new User(username, passwordEncoder.encode(password), "ADMIN", true));
        log.info("dev 管理者ユーザを作成しました: {} （パスワードは app.admin.default-password）", username);
    }
}
