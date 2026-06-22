package jp.kcgi.bousai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * SQLite(dev) のデータベースファイル用ディレクトリを起動初期に作成する。
 *
 * <p>{@code spring.datasource.url} が file ベースの SQLite ({@code jdbc:sqlite:<path>}) の場合のみ、
 * その親ディレクトリを作成する。これにより作業ディレクトリ（IDE 実行・CLI 実行で異なる）に依存せず
 * 「SQLITE_CANTOPEN: unable to open database file」を防ぐ。PostgreSQL 等では何もしない。</p>
 *
 * <p>登録は {@code META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor.imports}。</p>
 */
public class SqliteDataDirectoryPreparer implements EnvironmentPostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(SqliteDataDirectoryPreparer.class);
    private static final String SQLITE_PREFIX = "jdbc:sqlite:";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("spring.datasource.url");
        if (url == null || !url.startsWith(SQLITE_PREFIX)) {
            return;
        }

        String pathPart = url.substring(SQLITE_PREFIX.length());
        // インメモリ DB（:memory: や file::memory:）はディレクトリ不要
        if (pathPart.isBlank() || pathPart.contains(":memory:")) {
            return;
        }

        try {
            Path dbFile = Paths.get(pathPart).toAbsolutePath();
            Path parent = dbFile.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
                log.info("SQLite データディレクトリを作成しました: {}", parent);
            }
        } catch (Exception ex) {
            // 作成に失敗しても起動は継続（後続の DB 接続でエラーになれば原因が分かる）
            log.warn("SQLite データディレクトリの作成に失敗しました: {}", ex.getMessage());
        }
    }

    @Override
    public int getOrder() {
        // ConfigData（application*.properties）読込後に動かすため低優先度にする
        return Ordered.LOWEST_PRECEDENCE;
    }
}
