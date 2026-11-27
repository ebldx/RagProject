import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        final URI DEFAULT_URI = URI.create("http://localhost:11434/");
        final HttpClient DEFAULT_CLIENT = HttpClient.newHttpClient();

        String fileName = "";
        String embedModelName = "";
        String langModelName = "";
        String userPrompt = "";
        do {
            System.out.print("Enter filename of text file: ");
            fileName = scanner.nextLine();
        } while (!(new File(fileName).exists()));

        do {
            System.out.print("Enter name of embedding model: ");
            embedModelName = scanner.nextLine();
        } while (!AI.checkModel(embedModelName, AI.SupportedModelTypes.EMBEDDING, DEFAULT_CLIENT, DEFAULT_URI)[1]);

        do {
            System.out.print("Enter name of language model: ");
            langModelName = scanner.nextLine();
        } while (!AI.checkModel(langModelName, AI.SupportedModelTypes.LANGUAGE, DEFAULT_CLIENT, DEFAULT_URI)[1]);

        do {
            System.out.print("Enter user prompt: ");
            userPrompt = scanner.nextLine();
        } while (userPrompt.isBlank());
        scanner.close();
        System.out.println("");

        LanguageAI LLM = new LanguageAI(DEFAULT_CLIENT, DEFAULT_URI, langModelName, AI.SupportedModelTypes.LANGUAGE);
        EmbeddingAI VSM = new EmbeddingAI(DEFAULT_CLIENT, DEFAULT_URI, embedModelName, AI.SupportedModelTypes.EMBEDDING);

        ArrayList<String> textFileContents = new ArrayList<String>();
        File textFile = new File(fileName);
        Scanner fileReader = new Scanner(textFile);
        while (fileReader.hasNextLine()) {
            String nextLine = fileReader.nextLine();
            textFileContents.add(nextLine);
        }
        fileReader.close();

        Vector[] vectorData = new Vector[textFileContents.size()];
        for (int i = 0; i < textFileContents.size(); i++) {
            System.out.printf("Embedding line %s of %s\n", i + 1, textFileContents.size());
            vectorData[i] = new Vector(VSM.invokeModel(textFileContents.get(i)));
        }
        Vector userPromptVector = new Vector(VSM.invokeModel(userPrompt));

        ArrayList<Double> similarities = new ArrayList<Double>();
        for (Vector thisVectorData : vectorData) {
            similarities.add(Vector.vectorSimilarity(userPromptVector, thisVectorData));
        }

        // Use maps instead?
        System.out.println("\nCalculating similarity...");
        for (int i = 0; i < similarities.size(); i++) {
            double similarity = similarities.get(i);
            String text = textFileContents.get(i);
            if (similarity >= similarities.getFirst()) {
                similarities.remove(i);
                textFileContents.remove(i);
                similarities.addFirst(similarity);
                textFileContents.addFirst(text);
            }
        }

        String retrievedData = "";
        for (int i = 0; i < Math.min(3, textFileContents.size()); i++) {
            retrievedData += "\n\t" + textFileContents.get(i);
        }

        // Add calculation to determine how much info from vector similarity to provide.
        String systemPrompt = "You are a helpful assistant. Answer the users question using the rules reference information provided below.\nRules:\n- Treat the reference information as your primary source.\n - If the reference information is not enough to provide an answer, say so clearly.\n - If the information provided is conflicting or unclear, point that out.\n - Clearly distinguish general information from the reference information when providing an answer.\nReference information:\n" + retrievedData;
        System.out.println("\n\n\n\nReference info:" + retrievedData + "\n\nUser prompt:\n\t" + userPrompt);
        System.out.println("\nRunning language model...");
        System.out.println(LLM.invokeModel(systemPrompt, userPrompt));
    }
}