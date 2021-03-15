import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Program {
    public static void main(String[] args) throws IOException {
        String file = "/Users/petrkireev/IdeaProjects/Simple Banking System/Problems/Get the game mode/src/dataset_91065.txt";
        Scanner sc = new Scanner(new File(file));

        int count = 0;
        while (sc.hasNext()) {
            int n = sc.nextInt();
            if (n == 0) break;
            if (n % 2 == 0) {
                count++;
            }
        }

        sc.close();
        System.out.println(count);
    }
}
