import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AI {
    private final HttpClient client;
    private final URI baseURI;
    public AI() {
        this(HttpClient.newHttpClient(), URI.create("http://localhost:11434/"));
    }
    public AI(HttpClient client, URI baseURI) {
        this.client = client;
        this.baseURI = baseURI;
    }

    public String languageModel (String modelName, String systemPrompt, String userPrompt) throws IOException, InterruptedException {
        //startModel(modelName);
        JSONObject json = new JSONObject();
        json.put("model", modelName);
        json.put("system", systemPrompt);
        json.put("prompt", userPrompt);
        json.put("stream", false);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(baseURI.resolve("api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
            .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBody = new JSONObject(response.body().toString());
        try {
            return responseBody.get("response").toString();
        } catch (JSONException e) {
            String errorResponse = responseBody.getString("error");
            throw new RuntimeException(errorResponse);
        }
    }
    public double[] embeddingModel (String modelName, String inputText) throws IOException, InterruptedException, RuntimeException {
        //startModel(modelName);
        JSONObject json = new JSONObject();
        json.put("model", modelName);
        json.put("input", inputText);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(baseURI.resolve("api/embed"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBody = new JSONObject(response.body().toString());
        JSONArray embeddings;
        try {
            embeddings = (JSONArray) responseBody.getJSONArray("embeddings").get(0);
        } catch (JSONException e) {
            String errorResponse = responseBody.getString("error");
            throw new RuntimeException(errorResponse);
        }

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