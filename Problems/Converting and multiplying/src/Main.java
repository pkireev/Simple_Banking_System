import java.util.Scanner;
import java.util.ArrayList;
class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ArrayList<String> seq = new ArrayList<>();

        while (true) {
            String num = sc.nextLine();
            if ("0".equals(num)) {
                break;
            }
            seq.add(num);
        }

        for (String el : seq) {
            try {
                System.out.println(Integer.parseInt(el) * 10);
            } catch (Exception e) {
                System.out.printf("Invalid user input: %s\n", el);
            }
        }

    }
}