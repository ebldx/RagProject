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

        String[] textFileContents = new String[200]; // FIND TEXTFILE SIZE!
        File textFile = new File(fileName);
        Scanner fileReader = new Scanner(textFile);
        int lineNr = 0;
        while (fileReader.hasNextLine()) {
            String nextLine = fileReader.nextLine();
            textFileContents[lineNr] = nextLine;
            lineNr++;
        }
        fileReader.close();

        Vector[] vectorData = new Vector[textFileContents.length];
        for (int i = 0; i < textFileContents.length; i++) {
            vectorData[i] = new Vector(AI.embeddingModel(embedModelName, textFileContents[i]));
        }
        Vector userPromptVector = new Vector(AI.embeddingModel(embedModelName, userPrompt));

        double[] similarities = new double[vectorData.length];
        for (int i = 0; i < vectorData.length; i++) {
            similarities[i] = Vector.vectorSimilarity(userPromptVector, vectorData[i]);
        }

        Arrays.sort(similarities);
    }
}