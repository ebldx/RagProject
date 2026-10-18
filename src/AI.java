import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class AI {
    public static String languageModel (String modelName, String systemPrompt, String userPrompt) throws IOException, InterruptedException {
        startModel(modelName);
        JSONObject json = new JSONObject();
        json.put("model", modelName);
        json.put("system", systemPrompt);
        json.put("prompt", userPrompt);
        json.put("stream", false);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:11434/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        HttpResponse response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBody = (JSONObject) JSONValue.parse(response.body());
        return responseBody.get("response").toString();
    }
    public static double[] embeddingModel (String modelName, String inputText) throws IOException, InterruptedException {
        startModel(modelName);
        JSONObject json = new JSONObject();
        json.put("model", modelName);
        json.put("input", inputText);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/embed"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBody = (JSONObject) JSONValue.parse(response.body());
        return (double[]) responseBody.get("embeddings");
    }

    // Unreliable, more checks needed.
    public static void startModel(String modelName) throws IOException {
        Process serve = new ProcessBuilder("ollama serve").start();
        Process pull = new ProcessBuilder("ollama pull", modelName).start();
        Process run = new ProcessBuilder("ollama run", modelName).start();
    }
}