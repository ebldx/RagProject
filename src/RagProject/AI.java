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
    public AI(String modelName, SupportedModelTypes modelType) throws IOException, InterruptedException {
        this.client = HttpClient.newHttpClient();
        this.baseURI = URI.create("http://localhost:11434/");
        this.modelType = modelType;
        this.modelName = modelName;
        this.prepModel();
    }
    public AI(HttpClient client, URI baseURI, String modelName, SupportedModelTypes modelType) throws IOException, InterruptedException {
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
            boolean[] modelCheck = checkModel(this.modelName, this.modelType, this.client, this.baseURI);
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

    public static boolean[] checkModel(String modelName, SupportedModelTypes modelType, HttpClient client, URI baseURI) throws IOException, InterruptedException {
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

    public String getModelName() {return this.modelName;}
    public SupportedModelTypes getModelType() {return this.modelType;}
    public HttpClient getClient() {return this.client;}
    public URI getBaseURI() {return this.baseURI;}
}