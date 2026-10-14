import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        String fileName = "";
        do {
            System.out.println("Enter filename of text file: ");
            fileName = scanner.nextLine();
        } while (!(new File(fileName).exists()));

        String embedModelName = "";
        do {
            System.out.println("Enter name of embedding model: ");
            embedModelName = scanner.nextLine();
        } while (embedModelName == "");

        String langModelName = "";
        do {
            System.out.println("Enter name of language model: ");
            langModelName = scanner.nextLine();
        } while (langModelName == "");

        String userPrompt = "";
        do {
            System.out.println("Enter user prompt: ");
            userPrompt = scanner.nextLine();
        } while (userPrompt == "");
        scanner.close();

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
            vectorData[i] = new Vector(AI.embeddingModel(embedModelName, textFileContents.get(i)));
        }
        Vector userPromptVector = new Vector(AI.embeddingModel(embedModelName, userPrompt));

        ArrayList<Double> similarities = new ArrayList<Double>();
        for (Vector thisVectorData : vectorData) {
            similarities.add(Vector.vectorSimilarity(userPromptVector, thisVectorData));
        }

        // Use maps instead?
        for (int i = 0; i < similarities.size(); i++) {
            double similarity = similarities.get(i);
            String text = textFileContents.get(i);
            if (similarity <= similarities.getFirst()) {
                similarities.remove(i);
                textFileContents.remove(i);
                similarities.addFirst(similarity);
                textFileContents.addFirst(text);
            }
        }


        // Add calculation to determine how much info from vector similarity to provide.
        String systemPrompt = String.format("""
                    You are a helpful assistant. Answer the user's question using the rules reference information provided below.
                
                    Rules:
                    - Treat the reference information as your primary source.
                    - Do not invent facts or fill gaps with unsupported assumptions.
                    - If the reference information is not enough to provide an answer, say so clearly.
                    - If the information provided is conflicting or unclear, point that out.
                    - Clearly distinguish general information from the reference information when providing an answer.
                    - Keep the answer focused and concise.
                
                    Reference information:
                    %s
                    %s
                    %s
                """, textFileContents.get(0), textFileContents.get(1), textFileContents.get(2));

        System.out.println(AI.languageModel(langModelName, systemPrompt, userPrompt));
    }
}