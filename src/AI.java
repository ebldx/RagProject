import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AI {
    public static String languageModel (String modelName, String systemPrompt, String userPrompt) throws IOException, InterruptedException {
        startModel(modelName);
        String json = String.format("""
                {"model":%s, "system":"%s", "prompt":"%s", "stream":false}
                """, modelName, systemPrompt, userPrompt);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:11434/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        HttpResponse response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        return response.body().toString();
    }
    public static double[] embeddingModel (String modelName, String inputText) throws IOException, InterruptedException {
        startModel(modelName);
        String json = String.format("""
                {"model":"%s","input":"%s"}
                """, modelName, inputText);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        return (double[]) response.body();
    }

    // Unreliable, more checks needed.
    public static void startModel(String modelName) throws IOException {
        Process serve = new ProcessBuilder("ollama serve").start();
        Process pull = new ProcessBuilder("ollama pull", modelName).start();
        Process run = new ProcessBuilder("ollama run", modelName).start();
    }
}