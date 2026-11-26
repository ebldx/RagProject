import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class EmbeddingAI extends AI {
    public EmbeddingAI(String modelName, SupportedModelTypes modelType) throws IOException, InterruptedException {
        super(modelName, modelType);
    }
    public EmbeddingAI(HttpClient client, URI baseURI, String modelName, SupportedModelTypes modelType) throws IOException, InterruptedException {
        super(client, baseURI, modelName, modelType);
    }

    public double[] invokeModel(String inputText) throws IOException, InterruptedException, RuntimeException {
        if (this.getModelType() != SupportedModelTypes.EMBEDDING) {
            throw new RuntimeException("Invalid model type for VectorAI class.");
        }
        JSONObject json = new JSONObject();
        json.put("model", this.getModelName());
        json.put("input", inputText);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(this.getBaseURI().resolve("api/embed"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        HttpResponse<String> response = this.getClient().send(request, HttpResponse.BodyHandlers.ofString());

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

