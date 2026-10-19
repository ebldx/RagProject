import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class AI {
    public static String languageModel (String modelName, String systemPrompt, String userPrompt) throws IOException, InterruptedException {
        //startModel(modelName);
        JSONObject json = new JSONObject();
        json.put("model", modelName);
        json.put("system", systemPrompt);
        json.put("prompt", userPrompt);
        json.put("stream", false);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:11434/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
            .build();
        HttpResponse response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBody = new JSONObject(response.body().toString());
        return responseBody.get("response").toString();
    }
    public static double[] embeddingModel (String modelName, String inputText) throws IOException, InterruptedException {
        //startModel(modelName);
        JSONObject json = new JSONObject();
        json.put("model", modelName);
        json.put("input", inputText);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/embed"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        HttpResponse response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBody = new JSONObject(response.body().toString());
        JSONArray embeddings = (JSONArray) responseBody.getJSONArray("embeddings").get(0);

        double[] output = new double[embeddings.length()];
        for (int i = 0; i < embeddings.length(); i++) {
            output[i] = embeddings.getDouble(i);
        }
        return output;
    }

    // Unreliable, more checks needed.
    public static void startModel(String modelName) throws IOException {
        Process serve = new ProcessBuilder("ollama serve").start();
        Process pull = new ProcessBuilder("ollama pull", modelName).start();
        Process run = new ProcessBuilder("ollama run", modelName).start();
    }
}