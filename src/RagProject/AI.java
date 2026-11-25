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
    private final SupportedModelTypes modelType;
    private final String modelName;
    private boolean modelRunning;
    public AI(String modelName, SupportedModelTypes modelType) {
        this(HttpClient.newHttpClient(), URI.create("http://localhost:11434/"));
        this.modelType = modelType;
        this.modelName = modelName;
        this.prepModel();
    }
    public AI(HttpClient client, URI baseURI, String modelName, SupportedModelTypes modelType) {
        this.client = client;
        this.baseURI = baseURI;
        this.modelType = modelType;
        this.modelName = modelName;
        this.prepModel();
    }


    private void prepModel() throws IOException, InterruptedException {
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
                    if (this.modelName.equals(model.getString("name"))) {
                        modelRunning = true;
                    }
                }
            }
        }

        if (!modelRunning) {
            boolean[] modelCheck = this.checkModel(this.modelName, this.modelType);
            boolean modelIsPulled = modelCheck[0];
            boolean modelIsType = modelCheck[1];
            if (!modelIsPulled) {
                System.out.println("Pulling model...");
                JSONObject json = new JSONObject();
                json.put("model", this.modelName);
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
                    throw new RuntimeException(String.format("Failed to pull model \"%s\", %s", this.modelName, pullResponseBody.getString("error")));
                } else {
                    throw new RuntimeException(String.format("Failed to pull model \"%s\", JSON output: %s", this.modelName, pullResponseBody));
                }
                if (!(pullStatus.equals("success"))) {
                    throw new RuntimeException(String.format("Failed to pull model \"%s\".", this.modelName));
                }
            }
        }
    }

    public static boolean[] checkModel(String modelName, SupportedModelTypes modelType) throws IOException, InterruptedException {
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


class VectorAI extends AI {
    public double[] invokeModel(String inputText) throws IOException, InterruptedException, RuntimeException {
        if (this.modelType != SupportedModelTypes.EMBEDDING) {
            throw new RuntimeException("Invalid model type for VectorAI class.");
        }
        JSONObject json = new JSONObject();
        json.put("model", this.modelName);
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
}


class LanguageAI extends AI {
    public String invokeModel(String systemPrompt, String userPrompt) throws IOException, InterruptedException {
        if (this.modelType != SupportedModelTypes.LANGUAGE) {
            throw new RuntimeException("Invalid model type for LanguageAI class.");
        }
        JSONObject json = new JSONObject();
        json.put("model", this.modelName);
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
}