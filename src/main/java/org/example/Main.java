package org.example;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            showHomeMenu();
            System.out.print("Choose an option: ");
            String homeOption = scanner.nextLine().trim().toUpperCase();

            switch (homeOption) {
                case "D":
                    addDeposit(scanner);
                    break;

                case "P":
                    makePayment(scanner);
                    break;

                case "L":
                    ledgerMenu(scanner);
                    break;

                case "X":
                    System.out.println("Goodbye! Have a great day!");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid option. Please enter D, P, L, or X.");
                    break;
            }
        }
    }

    // ─── Home Menu ───────────────────────────────────────────────────────────────

    public static void showHomeMenu() {
        System.out.println("\n===========================");
        System.out.println("         Home Menu          ");
        System.out.println("===========================");
        System.out.println("D) Add Deposit");
        System.out.println("P) Make Payment (Debit)");
        System.out.println("L) Ledger");
        System.out.println("X) Exit");
    }

    // ─── Add Deposit ─────────────────────────────────────────────────────────────

    public static void addDeposit(Scanner scanner) {
        System.out.println("\n--- Add Deposit ---");

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Vendor: ");
        String vendor = scanner.nextLine().trim();

        System.out.print("Amount (positive number): ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Deposit cancelled.");
            return;
        }

        if (amount <= 0) {
            System.out.println("Deposit amount must be positive. Deposit cancelled.");
            return;
        }

        Transaction deposit = new Transaction(
                LocalDate.now(),
                LocalTime.now().withNano(0),
                description,
                vendor,
                amount
        );

        FileManager.saveTransaction(deposit);
        System.out.printf("Deposit of $%.2f from %s saved successfully.%n", amount, vendor);
    }

    // ─── Make Payment ────────────────────────────────────────────────────────────

    public static void makePayment(Scanner scanner) {
        System.out.println("\n--- Make Payment (Debit) ---");

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Vendor: ");
        String vendor = scanner.nextLine().trim();

        System.out.print("Amount (positive number): ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Payment cancelled.");
            return;
        }

        if (amount <= 0) {
            System.out.println("Payment amount must be positive. Payment cancelled.");
            return;
        }

        // Store payments as negative
        Transaction payment = new Transaction(
                LocalDate.now(),
                LocalTime.now().withNano(0),
                description,
                vendor,
                -amount
        );

        FileManager.saveTransaction(payment);
        System.out.printf("Payment of $%.2f to %s saved successfully.%n", amount, vendor);
    }

    // ─── Ledger Menu ─────────────────────────────────────────────────────────────

    public static void ledgerMenu(Scanner scanner) {
        boolean ledgerOpen = true;

        while (ledgerOpen) {
            System.out.println("\n===========================");
            System.out.println("           Ledger           ");
            System.out.println("===========================");
            System.out.println("A) All entries");
            System.out.println("D) Deposits only");
            System.out.println("P) Payments only");
            System.out.println("R) Reports");
            System.out.println("H) Home");
            System.out.print("Select an option: ");

            String ledgerOption = scanner.nextLine().trim().toUpperCase();

            switch (ledgerOption) {
                case "A":
                    displayTransactions(FileManager.getTransactions());
                    break;

                case "D":
                    List<Transaction> deposits = FileManager.getTransactions().stream()
                            .filter(t -> t.getAmount() > 0)
                            .collect(Collectors.toList());
                    System.out.println("\n--- Deposits ---");
                    displayTransactions(deposits);
                    break;

                case "P":
                    List<Transaction> payments = FileManager.getTransactions().stream()
                            .filter(t -> t.getAmount() < 0)
                            .collect(Collectors.toList());
                    System.out.println("\n--- Payments ---");
                    displayTransactions(payments);
                    break;

                case "R":
                    reportsMenu(scanner);
                    break;

                case "H":
                    ledgerOpen = false;
                    break;

                default:
                    System.out.println("Invalid option. Please enter A, D, P, R, or H.");
                    break;
            }
        }
    }

    // ─── Reports Menu ────────────────────────────────────────────────────────────

    public static void reportsMenu(Scanner scanner) {
        boolean reportsOpen = true;

        while (reportsOpen) {
            System.out.println("\n===========================");
            System.out.println("           Reports          ");
            System.out.println("===========================");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("0) Back");
            System.out.print("Select an option: ");

            String reportOption = scanner.nextLine().trim();

            LocalDate today = LocalDate.now();

            switch (reportOption) {
                case "1": {
                    // Month To Date: from the 1st of current month to today
                    LocalDate startOfMonth = today.withDayOfMonth(1);
                    List<Transaction> results = filterByDateRange(FileManager.getTransactions(), startOfMonth, today);
                    System.out.println("\n--- Month To Date ---");
                    displayTransactions(results);
                    break;
                }

                case "2": {
                    // Previous Month: entire calendar month before this one
                    LocalDate firstOfPrevMonth = today.minusMonths(1).withDayOfMonth(1);
                    LocalDate lastOfPrevMonth = firstOfPrevMonth.withDayOfMonth(firstOfPrevMonth.lengthOfMonth());
                    List<Transaction> results = filterByDateRange(FileManager.getTransactions(), firstOfPrevMonth, lastOfPrevMonth);
                    System.out.println("\n--- Previous Month ---");
                    displayTransactions(results);
                    break;
                }

                case "3": {
                    // Year To Date: from Jan 1 of current year to today
                    LocalDate startOfYear = today.withDayOfYear(1);
                    List<Transaction> results = filterByDateRange(FileManager.getTransactions(), startOfYear, today);
                    System.out.println("\n--- Year To Date ---");
                    displayTransactions(results);
                    break;
                }

                case "4": {
                    // Previous Year: entire calendar year before this one
                    LocalDate startOfPrevYear = today.minusYears(1).withDayOfYear(1);
                    LocalDate endOfPrevYear = startOfPrevYear.withDayOfYear(startOfPrevYear.lengthOfYear());
                    List<Transaction> results = filterByDateRange(FileManager.getTransactions(), startOfPrevYear, endOfPrevYear);
                    System.out.println("\n--- Previous Year ---");
                    displayTransactions(results);
                    break;
                }

                case "5": {
                    System.out.print("Enter vendor name to search: ");
                    String vendorSearch = scanner.nextLine().trim().toLowerCase();
                    List<Transaction> results = FileManager.getTransactions().stream()
                            .filter(t -> t.getVendor().toLowerCase().contains(vendorSearch))
                            .collect(Collectors.toList());
                    System.out.println("\n--- Vendor Search: " + vendorSearch + " ---");
                    displayTransactions(results);
                    break;
                }

                case "0":
                    reportsOpen = false;
                    break;

                default:
                    System.out.println("Invalid option. Please enter 0-5.");
                    break;
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    public static void displayTransactions(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        // Newest entries first
        transactions.sort(Comparator.comparing(Transaction::getDate)
                .thenComparing(Transaction::getTime)
                .reversed());

        System.out.println(String.format("%-12s | %-8s | %-30s | %-15s | %s",
                "Date", "Time", "Description", "Vendor", "Amount"));
        System.out.println("-".repeat(85));

        for (Transaction t : transactions) {
            System.out.println(t);
        }
    }

    public static List<Transaction> filterByDateRange(List<Transaction> transactions, LocalDate start, LocalDate end) {
        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .collect(Collectors.toList());
    }
}
