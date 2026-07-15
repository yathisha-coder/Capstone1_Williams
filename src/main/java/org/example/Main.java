package org.example;

import de.vandermeer.asciitable.AsciiTable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
                    displayFormat(FileManager.getTransactions());
                    break;

                case "D":
                    List<Transaction> deposits = FileManager.getTransactions().stream()
                            .filter(t -> t.getAmount() > 0)
                            .collect(Collectors.toList());
                    System.out.println("\n--- Deposits ---");
                    displayFormat(deposits);
                    break;

                case "P":
                    List<Transaction> payments = FileManager.getTransactions().stream()
                            .filter(t -> t.getAmount() < 0)
                            .collect(Collectors.toList());
                    System.out.println("\n--- Payments ---");
                    displayFormat(payments);
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
            System.out.println("6) Custom Search");
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
                    displayFormat(results);
                    break;
                }

                case "2": {
                    // Previous Month: entire calendar month before this one
                    LocalDate firstOfPrevMonth = today.minusMonths(1).withDayOfMonth(1);
                    LocalDate lastOfPrevMonth = firstOfPrevMonth.withDayOfMonth(firstOfPrevMonth.lengthOfMonth());
                    List<Transaction> results = filterByDateRange(FileManager.getTransactions(), firstOfPrevMonth, lastOfPrevMonth);
                    System.out.println("\n--- Previous Month ---");
                    displayFormat(results);
                    break;
                }

                case "3": {
                    // Year To Date: from Jan 1 of current year to today
                    LocalDate startOfYear = today.withDayOfYear(1);
                    List<Transaction> results = filterByDateRange(FileManager.getTransactions(), startOfYear, today);
                    System.out.println("\n--- Year To Date ---");
                    displayFormat(results);
                    break;
                }

                case "4": {
                    // Previous Year: entire calendar year before this one
                    LocalDate startOfPrevYear = today.minusYears(1).withDayOfYear(1);
                    LocalDate endOfPrevYear = startOfPrevYear.withDayOfYear(startOfPrevYear.lengthOfYear());
                    List<Transaction> results = filterByDateRange(FileManager.getTransactions(), startOfPrevYear, endOfPrevYear);
                    System.out.println("\n--- Previous Year ---");
                    displayFormat(results);
                    break;
                }

                case "5": {
                    System.out.print("Enter vendor name to search: ");
                    String vendorSearch = scanner.nextLine().trim().toLowerCase();
                    List<Transaction> results = FileManager.getTransactions().stream()
                            .filter(t -> t.getVendor().toLowerCase().contains(vendorSearch))
                            .collect(Collectors.toList());
                    System.out.println("\n--- Vendor Search: " + vendorSearch + " ---");
                    displayFormat(results);
                    break;
                }
                case "6":
                    runCustomSearch(scanner);
                    break;

                case "0":
                    reportsOpen = false;
                    break;

                default:
                    System.out.println("Invalid option. Please enter 0, 1, 2, 3, 4, 5, or 6.");
                    break;
            }
        }
    }

    // ─── Custom Search Engine ──────────────────────────────────────────────────

    private static void runCustomSearch(Scanner scanner) {
        System.out.println("\n--- Custom Search ---");
        System.out.println("(Press Enter to skip any field)");

        LocalDate startDate = null;
        System.out.print("Start Date (YYYY-MM-DD): ");
        String startInput = scanner.nextLine().trim();
        if (!startInput.isEmpty()) {
            try {
                startDate = LocalDate.parse(startInput);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Skipping start date filter.");
            }
        }

        LocalDate endDate = null;
        System.out.print("End Date (YYYY-MM-DD): ");
        String endInput = scanner.nextLine().trim();
        if (!endInput.isEmpty()) {
            try {
                endDate = LocalDate.parse(endInput);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Skipping end date filter.");
            }
        }

        System.out.print("Description: ");
        String descInput = scanner.nextLine().trim();

        System.out.print("Vendor: ");
        String vendorInput = scanner.nextLine().trim();

        Double targetAmount = null;
        System.out.print("Amount: ");
        String amountInput = scanner.nextLine().trim();
        if (!amountInput.isEmpty()) {
            try {
                targetAmount = Double.parseDouble(amountInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount format. Skipping amount filter.");
            }
        }

        List<Transaction> allTransactions = FileManager.getTransactions();

        final LocalDate finalStart = startDate;
        final LocalDate finalEnd = endDate;
        final Double finalAmount = targetAmount;

        List<Transaction> filteredResults = allTransactions.stream()
                .filter(t -> finalStart == null || !t.getDate().isBefore(finalStart))
                .filter(t -> finalEnd == null || !t.getDate().isAfter(finalEnd))
                .filter(t -> descInput.isEmpty() || t.getDescription().toLowerCase().contains(descInput.toLowerCase()))
                .filter(t -> vendorInput.isEmpty() || t.getVendor().toLowerCase().contains(vendorInput.toLowerCase()))
                .filter(t -> finalAmount == null || Math.abs(t.getAmount() - finalAmount) < 0.001)
                .collect(Collectors.toList());

        System.out.println("\n--- Custom Search Results ---");
        //displayTransactions(filteredResults);
        displayFormat(allTransactions);
    }
    // ─── Helpers ─────────────────────────────────────────────────────────────────

    /*public static void displayTransactions(List<Transaction> transactions) {
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
            }*/

    public static List<Transaction> filterByDateRange(List<Transaction> transactions, LocalDate start, LocalDate end) {
        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .collect(Collectors.toList());
    }
    public static void displayFormat(List<Transaction> transactions) {

        AsciiTable at = new AsciiTable();

        // Header
        at.addRule();
        //Add the table header
        at.addRow("Date", "Time", "Description", "Vendor", "Amount");
        at.addRule();

        // Rows
        for (Transaction t : transactions) {
            at.addRow(
                    t.getDate(),
                    t.getTime(),
                    t.getDescription(),
                    t.getVendor(),
                    String.format("%.2f", t.getAmount())
            );

            at.addRule();
        }

        // Print table
        System.out.println(at.render());
    }
}
