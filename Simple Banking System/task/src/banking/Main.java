package banking;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static Scanner sc = new Scanner(System.in);
    static private int id = 1;
    static private String cardNumber;
    static private long balance;
    static private String pin;
    static boolean isLogged = false;
    static String dbFile = "./cards.s3db";

    public static void main(String[] args) {
        if (args.length >= 2) {
            if ("-fileName".equals(args[0])) {
                dbFile = args[1];
            }
        }

        intitializeDb();

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

        insertCardDataIntoDb();
    }

    static boolean logIn() {
        System.out.println("Enter your card number:");
        String getCard = sc.nextLine();
        System.out.println("Enter your PIN:");
        String getPin = sc.nextLine();

        balance = getCardDataFromDb(getCard, getPin);

        if (balance >= 0) {
            System.out.println("You successfully logged in!\n");
            return true;
        } else {
            System.out.println("Wrong card number or PIN!\n");
            return false;
        }


//        if (getCard.equals(cardNumber) && getPin.equals(pin)) {
//            System.out.println("You successfully logged in!\n");
//            return true;
//        } else {
//            System.out.println("Wrong card number or PIN!\n");
//            return false;
//        }
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

    public static void intitializeDb() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbFile);

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card (" +
                        "id INTEGER," +
                        "number TEXT," +
                        "pin TEXT," +
                        "balance INTEGER DEFAULT 0" +
                        ");");
                ResultSet rs = statement.executeQuery("SELECT id FROM card ORDER BY id DESC LIMIT 1;");
                if (rs.next()) {
                    id = rs.getInt("id");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertCardDataIntoDb() {
        String sql = String.format("INSERT INTO card (id, number, pin) VALUES (%d, %s, %s);", id++, cardNumber, pin);
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbFile);

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static long getCardDataFromDb(String enteredNumber, String enteredPin) {
        String sql = String.format("SELECT * FROM card WHERE number = %s", enteredNumber);
        long accountBalance = -1;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbFile);

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                ResultSet rs = statement.executeQuery(sql);
                while (rs.next()) {
                    if (rs.getString("pin").equals(enteredPin)) {
                        accountBalance = rs.getLong("balance");
                        break;
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accountBalance;
    }
}