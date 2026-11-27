import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class AITest {
    private HttpServer testServer;
    private HttpClient testClient;
    private AI testAi;
    private URI baseURI;
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

        testClient = HttpClient.newHttpClient();

        baseURI = URI.create("http://localhost:" + testServer.getAddress().getPort());

        responseBody.set("{\"models\":[{\"name\":\"test-model\"}]}");
        testAi = new AI(HttpClient.newHttpClient(), baseURI, "test-model", AI.SupportedModelTypes.NEITHER);
    }

    @AfterEach
    void tearDown() {
        if (testServer != null) {
            testServer.stop(0);
        }
    }

    @Test
    void prepModelRunningTest() throws IOException, InterruptedException {
        responseBody.set("{\"models\":[{\"name\":\"test-model\"}]}");
        assertDoesNotThrow(() -> new AI(HttpClient.newHttpClient(), baseURI, "test-model", AI.SupportedModelTypes.NEITHER));
    }

    @Test
    void prepModelNoneRunningTest() throws IOException, InterruptedException {
        responseBody.set("{\"models\":[]}");
        assertThrows(RuntimeException.class, () -> new AI(HttpClient.newHttpClient(), baseURI, "test-model", AI.SupportedModelTypes.NEITHER));
    }

    @Test
    void checkModelTest() throws IOException, InterruptedException {
        responseBody.set("{\"capabilities\":[\"embedding\"]}");
        assertFalse(testAi.checkModel("embedding-model", AI.SupportedModelTypes.LANGUAGE, testClient, baseURI)[1]);
        assertTrue(testAi.checkModel("embedding-model", AI.SupportedModelTypes.EMBEDDING, testClient, baseURI)[1]);

        responseBody.set("{\"capabilities\":[\"completion\"]}");
        assertTrue(testAi.checkModel("language-model", AI.SupportedModelTypes.LANGUAGE, testClient, baseURI)[1]);
        assertFalse(testAi.checkModel("language-model", AI.SupportedModelTypes.EMBEDDING, testClient, baseURI)[1]);
    }

    @Test
    void checkModelErrorTest() throws IOException, InterruptedException {
        responseBody.set("{\"error\":\"test text. :)\"}");
        assertFalse(testAi.checkModel("language-model", AI.SupportedModelTypes.LANGUAGE, testClient, baseURI)[0]);
        assertFalse(testAi.checkModel("embedding-model", AI.SupportedModelTypes.EMBEDDING, testClient, baseURI)[0]);
    }
}