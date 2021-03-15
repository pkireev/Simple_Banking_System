package banking;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static Scanner sc = new Scanner(System.in);
    static private String cardNumber;
    static private long balance;
    static private String pin;
    static boolean isLogged = false;

    public static void main(String[] args) {
        while (true) {
            if (isLogged) {
                printMenu2();

                int option = getOption();
                switch (option) {
                    case 0:
                        System.out.println("Bye!");
                        sc.close();
                        return;
                    case 1:
                        printBalance();
                        break;
                    case 2:
                        logOut();
                        break;
                }

            } else {
                printMenu();

                int option = getOption();

                switch (option) {
                    case 0:
                        sc.close();
                        return;
                    case 1:
                        createAccount();
                        break;
                    case 2:
                        isLogged = logIn();
                        break;
                }
            }
        }
    }

    static void printMenu() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }

    static void printMenu2() {
        System.out.println("1. Balance");
        System.out.println("2. Log out");
        System.out.println("0. Exit");
    }

    static int getOption() {
        int option = sc.nextInt();
        sc.nextLine();
        return option;
    }

    static void createAccount() {
        String newCard = "400000";
        String newPin = "";
        Random r = new Random(System.nanoTime());

        for (int i = 0; i < 9; i++) {
            newCard += String.valueOf(r.nextInt(10));
        }

        cardNumber = newCard;

        // creating the last number by Luhn algorithm
        addLastDigitByLuhn();

        for (int i = 0; i < 4; i++) {
            newPin += String.valueOf(r.nextInt(10));
        }

        pin = newPin;
        balance = 0;

        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(pin);
        System.out.println();
    }

    static boolean logIn() {
        System.out.println("Enter your card number:");
        String getCard = sc.nextLine();
        System.out.println("Enter your PIN:");
        String getPin = sc.nextLine();

        if (getCard.equals(cardNumber) && getPin.equals(pin)) {
            System.out.println("You successfully logged in!\n");
            return true;
        } else {
            System.out.println("Wrong card number or PIN!\n");
            return false;
        }
    }

    static void logOut() {
        isLogged = false;
        System.out.println("You successfully logged out!\n");
    }

    static void printBalance() {
        System.out.printf("Balance: %d\n\n", balance);
    }

    static void addLastDigitByLuhn() {
        int sum = 0;
        for (int i = 0; i < cardNumber.length(); i++) {
            char c = cardNumber.charAt(i);
            int value = Character.getNumericValue(c);

            if (i % 2 == 0) {
                value *= 2;
                if (value >= 10) {
                    value -= 9;
                }
            }
            sum += value;
        }

        int lastDigit = 0;
        if (sum % 10 != 0) {
            lastDigit = ((sum / 10) + 1) * 10 - sum;
        }

        cardNumber += lastDigit;

        // System.out.println(cardNumber + " " + sum + " " + lastDigit);
    }
}