import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        AI myAI = new AI();

        String fileName = "";
        String embedModelName = "";
        String langModelName = "";
        String userPrompt = "";
        boolean testing = false;
        if (!testing) {
                do {
                    System.out.print("Enter filename of text file: ");
                    fileName = scanner.nextLine();
                } while (!(new File(fileName).exists()));

                do {
                    System.out.print("Enter name of embedding model: ");
                    embedModelName = scanner.nextLine();
                } while (!myAI.checkModel(embedModelName, AI.SupportedModelTypes.EMBEDDING)[1]);

                do {
                    System.out.print("Enter name of language model: ");
                    langModelName = scanner.nextLine();
                } while (!myAI.checkModel(langModelName, AI.SupportedModelTypes.LANGUAGE)[1]);

                do {
                    System.out.print("Enter user prompt: ");
                    userPrompt = scanner.nextLine();
                } while (userPrompt == "");
                scanner.close();
                System.out.println("");
        } else {
            fileName = "text.txt";
            embedModelName = "qwen3-embedding:8b";
            langModelName = "llama3.1:8b";
            userPrompt = "Why do cats have whiskers?";
        }

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
            System.out.printf("Embedding line %s of %s\n", i+1, textFileContents.size());
            vectorData[i] = new Vector(myAI.embeddingModel(embedModelName, textFileContents.get(i)));
        }
        Vector userPromptVector = new Vector(myAI.embeddingModel(embedModelName, userPrompt));

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


        // Add calculation to determine how much info from vector similarity to provide.
        String systemPrompt = String.format("You are a helpful assistant. Answer the users question using the rules reference information provided below.\nRules:\n- Treat the reference information as your primary source.\n - If the reference information is not enough to provide an answer, say so clearly.\n - If the information provided is conflicting or unclear, point that out.\n - Clearly distinguish general information from the reference information when providing an answer.\nReference information:\n%s\n%s\n%s", textFileContents.get(0), textFileContents.get(1), textFileContents.get(2));
        System.out.println("\n\n\n\nReference info:\n\t" + textFileContents.get(0) + "\n\t" +  textFileContents.get(1) + "\n\t" + textFileContents.get(2) + "\n\nUser prompt:\n\t" + userPrompt);
        System.out.println("\nRunning language model...");
        System.out.println(myAI.languageModel(langModelName, systemPrompt, userPrompt));
    }
}