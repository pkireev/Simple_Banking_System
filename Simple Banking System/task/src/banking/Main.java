package banking;
import org.sqlite.SQLiteDataSource;

import java.sql.*;
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
    static Connection con;
    static Statement st;

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
                        closeAll();
                        return;
                    case 1:
                        printBalance();
                        break;
                    case 2:
                        addIncome();
                        break;
                    case 3:
                        doTransfer();
                        break;
                    case 4:
                        closeAccount();
                        break;
                    case 5:
                        logOut();
                        break;
                }

            } else {
                printMenu();

                int option = getOption();

                switch (option) {
                    case 0:
                        closeAll();
                        return;
                    case 1:
                        createAccount();
                        break;
                    case 2:
                        isLogged = logIn();
                        break;
                    case 7:
                        getAllInfoFromDb();
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
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }

    static int getOption() {
        int option = sc.nextInt();
        sc.nextLine();
        return option;
    }

    static void createAccount() {
        StringBuilder newCard = new StringBuilder("400000");
        StringBuilder newPin = new StringBuilder();
        Random r = new Random(System.nanoTime());

        for (int i = 0; i < 9; i++) {
            newCard.append(r.nextInt(10));
        }

        cardNumber = String.valueOf(newCard);

        // creating the last number by Luhn algorithm
        addLastDigitByLuhn();

        for (int i = 0; i < 4; i++) {
            newPin.append(r.nextInt(10));
        }

        pin = String.valueOf(newPin);
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

        boolean success = getCardDataFromDb(getCard, getPin);

        if (success) {
            System.out.println("You successfully logged in!\n");
            cardNumber = getCard;
        } else {
            System.out.println("Wrong card number or PIN!\n");
        }

        return success;
    }

    static void logOut() {
        isLogged = false;
        System.out.println("You successfully logged out!\n");
    }

    static void printBalance() {
        System.out.printf("Balance: %d\n\n", balance);
    }

    static void addIncome() {
        System.out.println("Enter income:");
        long income = sc.nextLong();

        // update balance
        balance += income;
        String sql = String.format("UPDATE card SET balance = %d WHERE number LIKE '%s'", balance, cardNumber);
        System.out.println(sql);

        try {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Income was added!");
    }

    static void doTransfer() {
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        String cardRecipient = sc.nextLine();

        if (!isCardCorrect(cardRecipient)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
        } else {
            if (!isCardExist(cardRecipient)) {
                System.out.println("Such a card does not exist.");
            } else {
                System.out.println("Enter how much money you want to transfer:");
                long transfer = sc.nextLong();
                if (transfer > balance) {
                    System.out.println("Not enough money!");
                } else {
                    doTransferTransaction(cardNumber, cardRecipient, transfer);
                    System.out.println("Success!");
                }
            }
        }

    }

    static void closeAccount() {
        String sqlClose = "DELETE FROM card WHERE number = ?";

        try {
            PreparedStatement stClose = con.prepareStatement(sqlClose);
            stClose.setString(1, cardNumber);
            stClose.executeUpdate();

            stClose.close();
            System.out.println("The account has been closed!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void doTransferTransaction(String sender, String recipient, long amount) {
        String sqlSender = "UPDATE card SET balance = balance - ? WHERE number = ?";
        String sqlRecipient = "UPDATE card SET balance = balance + ? WHERE number = ?";

        try {
            con.setAutoCommit(false);

            PreparedStatement stSender = con.prepareStatement(sqlSender);
            PreparedStatement stRecipient = con.prepareStatement(sqlRecipient);

            stSender.setLong(1, amount);
            stSender.setString(2, sender);

            stRecipient.setLong(1, amount);
            stRecipient.setString(2, recipient);

            stSender.executeUpdate();
            stRecipient.executeUpdate();

            con.commit();
            con.setAutoCommit(true);

            stSender.close();
            stRecipient.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static boolean isCardCorrect(String cardRecipient) {
        int sum = 0;
        for (int i = 0; i < cardRecipient.length() - 1; i++) {
            char c = cardRecipient.charAt(i);
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

        return Character.getNumericValue(cardRecipient.charAt(cardRecipient.length() - 1)) == lastDigit;
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
    }

    public static void intitializeDb() {
        try {
            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl("jdbc:sqlite:" + dbFile);
            con = dataSource.getConnection();

            st = con.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS card (" +
                    "id INTEGER," +
                    "number TEXT," +
                    "pin TEXT," +
                    "balance INTEGER DEFAULT 0" +
                    ");");
            ResultSet rs = st.executeQuery("SELECT id FROM card ORDER BY id DESC LIMIT 1;");
            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertCardDataIntoDb() {
        String sql = String.format("INSERT INTO card (id, number, pin) VALUES (%d, '%s', '%s');", id++, cardNumber, pin);

        try {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean getCardDataFromDb(String enteredNumber, String enteredPin) {
        String sql = String.format("SELECT * FROM card WHERE number LIKE '%s'", enteredNumber);
        boolean success = false;

        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                if (rs.getString("pin").equals(enteredPin)) {
                    balance = rs.getLong("balance");
                    success = true;
                    break;
                }
            }
        } catch (SQLException e) {
                e.printStackTrace();
        }

        return success;
    }

    public static boolean isCardExist(String enteredNumber) {
        String sql = String.format("SELECT * FROM card WHERE number LIKE '%s'", enteredNumber);
        boolean exists = false;

        try {
            ResultSet rs = st.executeQuery(sql);
            exists = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exists;
    }


    public static void closeAll() {
        sc.close();
        try {
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getAllInfoFromDb() {
        String sql = "SELECT * FROM card";

        try {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                System.out.printf("%d\t%s\t%s\t%d\n", rs.getInt("id"), rs.getString("number"), rs.getString("pin"), rs.getLong("balance"));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}