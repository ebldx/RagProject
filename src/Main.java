import java.util.Scanner;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        String fileName = "";
        do {
            System.out.println("Enter filename of text file:");
            fileName = scanner.nextLine();
        } while (!(new File(fileName).exists()));
        scanner.close();

        String[] textFileContents = new String[200]; // FIND TEXTFILE SIZE!
        File textFile = new File(fileName);
        Scanner fileReader = new Scanner(textFile);
        int i = 0;
        while (fileReader.hasNextLine()) {
            textFileContents[i] = fileReader.nextLine();
            i++;
        }
        fileReader.close();

        
    }
}