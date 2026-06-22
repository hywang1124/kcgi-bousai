package jp.kcgi.bousai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 主要 API のスモークテスト。実ポートを立てて HTTP で疎通を確認する。
 * リクエストボディは UTF-8 で送るため、日本語の質問でも文字化けしない。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiSmokeTest {

    @LocalServerPort
    private int port;

    private final HttpClient http = HttpClient.newHttpClient();

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void sheltersEndpointReturnsSeededData() throws Exception {
        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/shelters"))).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("nameJa"), "shelters response should contain nameJa");
    }

    @Test
    void chatEndpointReturnsAnswer() throws Exception {
        String body = "{\"question\":\"地震が来たらどうすればいい？\",\"lang\":\"ja\"}";
        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/chat")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"lang\":\"ja\""), "answer should be in ja");
        assertTrue(res.body().contains("\"answer\""), "response should contain an answer field");
    }

    @Test
    void chatEndpointRejectsBlankQuestion() throws Exception {
        String body = "{\"question\":\"  \"}";
        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/chat")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, res.statusCode());
    }

    @Test
    void adminEndpointRequiresAuthentication() throws Exception {
        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/admin/me"))).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(401, res.statusCode());
    }

    @Test
    void loginIssuesTokenAndGrantsAdminAccess() throws Exception {
        String loginBody = "{\"username\":\"admin\",\"password\":\"admin12345\"}";
        HttpResponse<String> login = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/auth/login")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(loginBody, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, login.statusCode());

        String token = extractJsonString(login.body(), "token");
        assertTrue(token != null && !token.isBlank(), "login should return a token");

        HttpResponse<String> me = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/admin/me")))
                        .header("Authorization", "Bearer " + token)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, me.statusCode());
        assertTrue(me.body().contains("ADMIN"), "admin should have the ADMIN role");
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        String body = "{\"username\":\"admin\",\"password\":\"nope\"}";
        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/auth/login")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(401, res.statusCode());
    }

    /** テスト用の簡易 JSON 文字列フィールド抽出。 */
    private static String extractJsonString(String json, String field) {
        String key = "\"" + field + "\":\"";
        int start = json.indexOf(key);
        if (start < 0) {
            return null;
        }
        start += key.length();
        int end = json.indexOf('"', start);
        return end < 0 ? null : json.substring(start, end);
    }
}
