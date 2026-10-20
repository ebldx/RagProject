import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.util.stream.StreamSupport.stream;


public class AI {
    public enum SupportedModelTypes {LANGUAGE, EMBEDDING}
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
        prepModel(modelName, SupportedModelTypes.LANGUAGE);
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
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBody = new JSONObject(response.body());
        try {
            return responseBody.get("response").toString();
        } catch (JSONException e) {
            String errorResponse = responseBody.getString("error");
            throw new RuntimeException(errorResponse);
        }
    }
    public double[] embeddingModel (String modelName, String inputText) throws IOException, InterruptedException, RuntimeException {
        prepModel(modelName, SupportedModelTypes.EMBEDDING);
        JSONObject json = new JSONObject();
        json.put("model", modelName);
        json.put("input", inputText);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(baseURI.resolve("api/embed"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBody = new JSONObject(response.body());
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

    public void prepModel(String modelName, SupportedModelTypes modelType) throws IOException, InterruptedException {
        Process serve = new ProcessBuilder("ollama", "serve").start();
        serve.waitFor();

        // Requests list of running models
        HttpRequest request = HttpRequest.newBuilder()
                .uri(baseURI.resolve("api/ps"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject responseBody = new JSONObject(response.body());

        boolean modelRunning = false;
        if (responseBody.has("models")) {
            JSONArray modelList = responseBody.getJSONArray("models");
            if (!modelList.isEmpty()) {
                for (int i = 0; i < modelList.length(); i++) {
                    JSONObject model = modelList.getJSONObject(i);
                    if (modelName.equals(model.getString("name"))) {
                        modelRunning = true;
                    }
                }
            }
        }

        if (!modelRunning) {
            boolean[] modelCheck = this.checkModel(modelName, modelType);
            boolean modelIsPulled = modelCheck[0];
            boolean modelIsType = modelCheck[1];
            if (!modelIsPulled) {
                System.out.println("Pulling model...");
                JSONObject json = new JSONObject();
                json.put("model", modelName);
                HttpRequest pullRequest = HttpRequest.newBuilder()
                        .uri(baseURI.resolve("api/pull"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();
                HttpResponse<String> pullResponse = client.send(pullRequest, HttpResponse.BodyHandlers.ofString());

                String pullStatus = "";
                JSONObject pullResponseBody = new JSONObject(pullResponse.body());
                if (pullResponseBody.has("status")) {
                    pullStatus = pullResponseBody.getString("status");
                    System.out.println(pullStatus.toUpperCase() + "!");
                } else if (pullResponseBody.has("error")) {
                    throw new RuntimeException(String.format("Failed to pull model \"%s\", %s", modelName, pullResponseBody.getString("error")));
                } else {
                    throw new RuntimeException(String.format("Failed to pull model \"%s\", JSON output: %s", modelName, pullResponseBody));
                }
                if (!(pullStatus.equals("success"))) {
                    throw new RuntimeException(String.format("Failed to pull model \"%s\".", modelName));
                }
            }
        }
    }

    public boolean[] checkModel(String modelName, SupportedModelTypes modelType) throws IOException, InterruptedException {
        JSONObject json = new JSONObject();
        json.put("model", modelName);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(baseURI.resolve("api/show"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBody = new JSONObject(response.body());

        boolean modelIsPulled = false;
        boolean modelIsType = false;
        if (responseBody.has("capabilities")) {
            modelIsPulled = true;
            JSONArray capabilities = responseBody.getJSONArray("capabilities");
            if (modelType == SupportedModelTypes.LANGUAGE) {
                modelIsType = capabilities.toList().contains("completion");
            } else if (modelType == SupportedModelTypes.EMBEDDING) {
                modelIsType = capabilities.toList().contains("embedding");
            }
        }
        return new boolean[] {modelIsPulled, modelIsType};
    }
}