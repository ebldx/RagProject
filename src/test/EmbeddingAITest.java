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

public class EmbeddingAITest {
    private HttpServer testServer;
    private EmbeddingAI testEmbedAi;
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
        responseBody.set("{\"models\":[{\"name\":\"embedding-model\"}]}");
        testEmbedAi = new EmbeddingAI(HttpClient.newHttpClient(), baseURI, "embedding-model", AI.SupportedModelTypes.EMBEDDING);
    }

    @AfterEach
    void tearDown() {
        if (testServer != null) {
            testServer.stop(0);
        }
    }

    @Test
    void invokeModelTest() throws IOException, InterruptedException {
        responseBody.set("{\"embeddings\":[[3.0, 4.0, 0.0]]}");
        double[] output = testEmbedAi.invokeModel("test text. :)");

        assertArrayEquals(new double[]{3.0, 4.0, 0.0}, output);
        assertEquals("/api/embed", requestPath.get());

        JSONObject requestJSON = new JSONObject(requestBody.get());
        assertEquals("embedding-model", requestJSON.getString("model"));
        assertEquals("test text. :)", requestJSON.getString("input"));
    }

    @Test
    void invokeModelErrorTest() {
        responseBody.set("{\"error\":\"error test. :)\"}");
        assertThrows(RuntimeException.class, () -> testEmbedAi.invokeModel("test text. :)"));
    }
}