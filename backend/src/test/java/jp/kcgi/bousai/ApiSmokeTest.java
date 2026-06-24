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
 * AI 聊天 API のスモークテスト。実ポートを立てて HTTP で疎通を確認する。
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
    void chatStreamEndpointReturnsSseChunks() throws Exception {
        String body = "{\"question\":\"地震が来たらどうすればいい？\",\"lang\":\"ja\"}";
        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(url("/api/v1/chat/stream")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("data:"), "should contain SSE data frames");
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
}
