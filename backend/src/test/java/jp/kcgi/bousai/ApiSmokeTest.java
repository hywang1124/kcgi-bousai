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

    @Test
    void hazardZonesEndpointReturnsSeededData() throws Exception {
        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/hazard-zones"))).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"geojson\""), "response should include geojson geometry");
        assertTrue(res.body().contains("FLOOD"), "response should include the seeded flood zone");
    }

    @Test
    void registerCreatesUserWithUserRole() throws Exception {
        String username = "user" + System.nanoTime();
        String body = "{\"username\":\"" + username + "\",\"password\":\"password123\"}";
        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/auth/register")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, res.statusCode());
        assertTrue(res.body().contains("\"role\":\"USER\""), "new user should default to USER role");
    }

    @Test
    void shelterWriteRequiresAuthentication() throws Exception {
        String body = "{\"nameJa\":\"テスト\",\"lat\":35.0,\"lng\":135.0}";
        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/shelters")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(401, res.statusCode());
    }

    @Test
    void adminCanCreateAndDeleteShelter() throws Exception {
        String token = adminToken();
        String body = "{\"nameJa\":\"テスト避難所\",\"lat\":35.01,\"lng\":135.76,\"capacity\":100,\"facilities\":[\"水\",\"毛布\"]}";
        HttpResponse<String> created = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/shelters")))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(201, created.statusCode());

        long id = extractJsonNumber(created.body(), "id");
        assertTrue(id > 0, "created shelter should have an id");

        HttpResponse<String> deleted = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/shelters/" + id)))
                        .header("Authorization", "Bearer " + token)
                        .DELETE().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(204, deleted.statusCode());
    }

    @Test
    void adminCanChangeUserRole() throws Exception {
        String username = "role" + System.nanoTime();
        String reg = "{\"username\":\"" + username + "\",\"password\":\"password123\"}";
        HttpResponse<String> registered = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/auth/register")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(reg, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        long id = extractJsonNumber(registered.body(), "id");

        String token = adminToken();
        HttpResponse<String> updated = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/admin/users/" + id + "/role")))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .PUT(HttpRequest.BodyPublishers.ofString("{\"role\":\"EDITOR\"}", StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, updated.statusCode());
        assertTrue(updated.body().contains("\"role\":\"EDITOR\""), "role should be updated to EDITOR");
    }

    private String adminToken() throws Exception {
        HttpResponse<String> login = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/auth/login")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                "{\"username\":\"admin\",\"password\":\"admin12345\"}", StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return extractJsonString(login.body(), "token");
    }

    /** テスト用の簡易 JSON 数値フィールド抽出。 */
    private static long extractJsonNumber(String json, String field) {
        String key = "\"" + field + "\":";
        int start = json.indexOf(key);
        if (start < 0) {
            return -1;
        }
        start += key.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        return end > start ? Long.parseLong(json.substring(start, end)) : -1;
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
