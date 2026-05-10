package UniSpace.util;

import java.util.Scanner;
import java.util.List;

public class ConsoleHelper {

    public static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Please enter a valid integer.");
            }
        }
    }

    public static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Please enter a valid number.");
            }
        }
    }

    public static String readNonEmpty(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("  [ERROR] Input cannot be empty.");
        }
    }

    public static void printTable(String title, List<String> rows) {
        System.out.println("\n  ── " + title + " ──");
        if (rows.isEmpty()) {
            System.out.println("    (No data)");
            return;
        }
        for (String row : rows) {
            System.out.println("    " + row);
        }
    }
}
