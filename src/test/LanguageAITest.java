import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LanguageAITest {
    private HttpServer testServer;
    private LanguageAI testLangAi;
    private AtomicReference<String> requestPath = new AtomicReference<>();
    private AtomicReference<String> requestBody = new AtomicReference<>();
    private AtomicReference<String> responseBody = new AtomicReference<>();

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        testServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        testServer.createContext("/", exchange -> {
            requestPath.set(exchange.getRequestURI().getPath());
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = responseBody.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        testServer.start();

        URI baseURI = URI.create("http://localhost:" + testServer.getAddress().getPort());
        responseBody.set("{\"models\":[{\"name\":\"language-model\"}]}");
        testLangAi = new LanguageAI(HttpClient.newHttpClient(), baseURI, "language-model", AI.SupportedModelTypes.LANGUAGE);
    }

    @AfterEach
    void tearDown() {
        if (testServer != null) {
            testServer.stop(0);
        }
    }

    @Test
    void langModelTest() throws IOException, InterruptedException {
        responseBody.set("{\"response\":\"response test. :)\"}");
        assertEquals("response test. :)", testLangAi.invokeModel( "system test text. :)", "test text. :)"));

        assertEquals("/api/generate", requestPath.get());

        JSONObject requestJSON = new JSONObject(requestBody.get());
        assertEquals("language-model", requestJSON.getString("model"));
        assertEquals("system test text. :)", requestJSON.getString("system"));
        assertEquals("test text. :)", requestJSON.getString("prompt"));
        assertFalse(requestJSON.getBoolean("stream"));
    }

    @Test
    void langModelErrorTest() {
        responseBody.set("{\"error\":\"error test. :)\"}");
        assertThrows(RuntimeException.class, () -> testLangAi.invokeModel("system test text. :)", "test text. :)"));
    }
}