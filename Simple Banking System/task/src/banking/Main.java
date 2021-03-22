package banking;
import org.sqlite.SQLiteDataSource;
import java.sql.*;
import java.util.Random;
import java.util.Scanner;


class DBController {
    private static DBController instance;
    private Connection con;
    private int id = 0;

    public static DBController getInstance(String dbFile) {
        if (instance == null) {
            instance = new DBController(dbFile);
        }
        return instance;
    }

    public static DBController getInstance() {
        return instance;
    }

    private DBController(String dbFile) {
        try {
            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl("jdbc:sqlite:" + dbFile);
            con = dataSource.getConnection();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!getId()) {
            createTable();
        }
    }

    public boolean getId() {
        String sqlGetId = "SELECT id FROM card ORDER BY id DESC LIMIT 1;";
        boolean success = false;

        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sqlGetId);

            if (rs.next()) {
                id = rs.getInt("id");
            }
            success = true;

            rs.close();
            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return success;
    }

    public long getBalance(String number) {
        long balance = 0;
        String sql = "SELECT balance FROM card WHERE number = ?";
        try {
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, number);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                balance = rs.getLong("balance");
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return balance;
    }

    public void createTable() {
        String sqlCreateTable =
                "CREATE TABLE IF NOT EXISTS card (" +
                        "id INTEGER," +
                        "number TEXT," +
                        "pin TEXT," +
                        "balance INTEGER DEFAULT 0" +
                        ");";

        try {
            Statement st = con.createStatement();
            st.executeUpdate(sqlCreateTable);

            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertCard(String number, String pin, long balance) {
        String sql = "INSERT INTO card (id, number, pin, balance) VALUES (?, ?, ?, ?);";

        try {
            PreparedStatement st = con.prepareStatement(sql);
            st.setInt(1, id++);
            st.setString(2, number);
            st.setString(3, pin);
            st.setLong(4, balance);

            st.executeUpdate();
            st.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isCardExist(String number) {
        String sql = "SELECT * FROM card WHERE number LIKE ?";
        boolean exists = false;

        try {
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, number);
            ResultSet rs = st.executeQuery();
            exists = rs.next();

            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exists;
    }

    public void addIncome(String number, long income) {
        String sql = "UPDATE card SET balance = balance + ? WHERE number LIKE ?";

        try {
            PreparedStatement st = con.prepareStatement(sql);
            st.setLong(1,income);
            st.setString(2, number);
            st.executeUpdate();

            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeAccount(String number) {
        String sqlClose = "DELETE FROM card WHERE number = ?";

        try {
            PreparedStatement stClose = con.prepareStatement(sqlClose);
            stClose.setString(1, number);
            stClose.executeUpdate();

            stClose.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void doTransferTransaction(String sender, String recipient, long amount) {
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

    public boolean isAuthorized(String number, String pin) {
        boolean success = false;
        String sql = "SELECT * FROM card WHERE number LIKE ? AND pin LIKE ?";

        try {
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, number);
            st.setString(2, pin);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                success = true;
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return success;
    }

    public void closeConnection() {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class Account {
    private String number;
    private String pin;
    private long balance;
    public boolean isLoggedIn;
    DBController db = DBController.getInstance();

    public void showMenu() {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }

    public Account(String number, String pin) {
        boolean success = db.isAuthorized(number, pin);
        if (success) {
            this.number = number;
            this.pin = pin;
            this.balance = db.getBalance(number);

            System.out.println("You successfully logged in!\n");
        } else {
            System.out.println("Wrong card number or PIN!\n");
        }

        this.isLoggedIn = success;
    }


    public Account() {
        StringBuilder newCard = new StringBuilder("400000");
        StringBuilder newPin = new StringBuilder();
        Random r = new Random(System.nanoTime());

        for (int i = 0; i < 9; i++) {
            newCard.append(r.nextInt(10));
        }

        number = String.valueOf(newCard);
        number += getLastDigitByLuhn(number);

        for (int i = 0; i < 4; i++) {
            newPin.append(r.nextInt(10));
        }

        pin = String.valueOf(newPin);
        balance = 0;

        db.insertCard(number, pin, balance);

        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(number);
        System.out.println("Your card PIN:");
        System.out.println(pin);
        System.out.println();
    }

    public void logOut() {
        isLoggedIn = false;
        System.out.println("You successfully logged out!\n");
    }

    public void printBalance() {
        System.out.printf("Balance: %d\n\n", balance);
    }

    public long getBalance() {
        return balance;
    }

    public int getLastDigitByLuhn(String number) {
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            char c = number.charAt(i);
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

        return lastDigit;
    }


    public boolean isCardCorrect(String number) {
        int lastDigitCalculated = getLastDigitByLuhn(number);
        int lastDigitConsidered = Character.getNumericValue(number.charAt(number.length() - 1));

        return lastDigitCalculated == lastDigitConsidered;
    }

    public void addIncome(long income) {
        db.addIncome(number, income);
        balance += income;

        System.out.println("Income was added!\n");
    }

    public void closeAccount() {
        isLoggedIn = false;
        db.closeAccount(number);
        System.out.println("The account has been closed!\n");
    }

    public void transferTo(String cardRecipient, long howMuch) {
        db.doTransferTransaction(this.number, cardRecipient, howMuch);
        balance -= howMuch;

        System.out.println("Success!\n");
    }
}


public class Main {
    static Scanner sc = new Scanner(System.in);
    static Account currentAccount = null;
    static DBController db;

    public static void main(String[] args) {

        String dbFile = "./cards.s3db";
        if (args.length >= 2) {
            if ("-fileName".equals(args[0])) {
                dbFile = args[1];
            }
        }

        db = DBController.getInstance(dbFile);

        while (true) {

            if (currentAccount == null || !currentAccount.isLoggedIn) {
                showMenu();
                int option = getOption();

                switch (option) {
                    case 0:
                        closeAll();
                        return;
                    case 1:
                        currentAccount = new Account();
                        break;
                    case 2:
                        logIn();
                        break;
                }
            } else {
                currentAccount.showMenu();
                int option = getOption();

                switch (option) {
                    case 0:
                        System.out.println("Bye!");
                        closeAll();
                        return;
                    case 1:
                        currentAccount.printBalance();
                        break;
                    case 2:
                        addIncome();
                        break;
                    case 3:
                        doTransfer();
                        break;
                    case 4:
                        currentAccount.closeAccount();
                        currentAccount = null;
                        break;
                    case 5:
                        currentAccount.logOut();
                        currentAccount = null;
                        break;
                }
            }
        }
    }

    static void showMenu() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }

    static int getOption() {
        int option = sc.nextInt();
        sc.nextLine();
        return option;
    }

    public static void logIn() {
        System.out.println("Enter your card number:");
        String number = sc.nextLine();
        System.out.println("Enter your PIN:");
        String pin = sc.nextLine();

        currentAccount = new Account(number, pin);
        if (!currentAccount.isLoggedIn) {
            currentAccount = null;
        }
    }

    public static void addIncome() {
        System.out.println("Enter income:");
        long income = sc.nextLong();

        currentAccount.addIncome(income);
    }


    static void doTransfer() {
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        String cardRecipient = sc.nextLine();

        if (!currentAccount.isCardCorrect(cardRecipient)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!\n");
        } else {
            if (!db.isCardExist(cardRecipient)) {
                System.out.println("Such a card does not exist.\n");
            } else {
                System.out.println("Enter how much money you want to transfer:");
                long howMuch = sc.nextLong();
                if (howMuch > currentAccount.getBalance()) {
                    System.out.println("Not enough money!\n");
                } else {
                    currentAccount.transferTo(cardRecipient, howMuch);
                }
            }
        }
    }

    public static void closeAll() {
        sc.close();
        db.closeConnection();
    }
}