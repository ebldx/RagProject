import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class LanguageAI extends AI {
    public LanguageAI(String modelName, SupportedModelTypes modelType) throws IOException, InterruptedException {
        super(modelName, modelType);
    }
    public LanguageAI(HttpClient client, URI baseURI, String modelName, SupportedModelTypes modelType) throws IOException, InterruptedException {
        super(client, baseURI, modelName, modelType);
    }

    public String invokeModel(String systemPrompt, String userPrompt) throws IOException, InterruptedException {
        if (this.getModelType() != SupportedModelTypes.LANGUAGE) {
            throw new RuntimeException("Invalid model type for LanguageAI class.");
        }
        JSONObject json = new JSONObject();
        json.put("model", this.getModelName());
        json.put("system", systemPrompt);
        json.put("prompt", userPrompt);
        json.put("stream", false);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(this.getBaseURI().resolve("api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        HttpResponse<String> response = this.getClient().send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBody = new JSONObject(response.body());
        try {
            return responseBody.get("response").toString();
        } catch (JSONException e) {
            String errorResponse = responseBody.getString("error");
            throw new RuntimeException(errorResponse);
        }
    }
}