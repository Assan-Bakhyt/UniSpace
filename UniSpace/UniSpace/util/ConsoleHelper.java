package UniSpace.util;

import UniSpace.enums.UserRole;
import UniSpace.model.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class ConsoleHelper {

    public static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            System.out.print(Colors.PURPLE);
            String line = scanner.nextLine().trim();
            System.out.print(Colors.RESET);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println(Colors.red("  [ERROR] Please enter a valid integer."));
            }
        }
    }

    public static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            System.out.print(Colors.PURPLE);
            String line = scanner.nextLine().trim();
            System.out.print(Colors.RESET);
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println(Colors.red("  [ERROR] Please enter a valid number."));
            }
        }
    }

    public static String readNonEmpty(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            System.out.print(Colors.PURPLE);
            String input = scanner.nextLine().trim();
            System.out.print(Colors.RESET);
            if (!input.isEmpty()) return input;
            System.out.println(Colors.red("  [ERROR] Input cannot be empty."));
        }
    }

    public static void printTable(String title, List<String> rows) {
        System.out.println(Colors.gray("\n  -- " + title + " --"));
        if (rows.isEmpty()) { System.out.println("    (No data)"); return; }
        for (String row : rows) System.out.println("    " + row);
    }

    public static void pressEnterToContinue(Scanner scanner) {
        System.out.print(Colors.gray("\n  Press Enter to continue..."));
        System.out.print(Colors.PURPLE);
        scanner.nextLine();
        System.out.print(Colors.RESET);
    }

    /** Reads a line of input and displays it in purple while typing. */
    public static String readChoice(Scanner scanner) {
        System.out.print(Colors.PURPLE);
        String line = scanner.nextLine().trim();
        System.out.print(Colors.RESET);
        return line;
    }

    /**
     * Shows a paginated, searchable user directory and lets the caller pick one user.
     *
     * @param users        all users in the system
     * @param excludeId    ID of the current user (excluded from list)
     * @param allowedRoles if not null/empty, only users with these roles are shown
     * @param scanner      scanner for input
     * @return selected User, or null if user pressed 0 (back/cancel)
     */
    public static User selectUserFromDirectory(Collection<User> users, String excludeId,
                                               Set<UserRole> allowedRoles, Scanner scanner) {
        List<User> list = users.stream()
                .filter(u -> !u.getId().equals(excludeId))
                .filter(u -> allowedRoles == null || allowedRoles.isEmpty()
                        || allowedRoles.contains(u.getRole()))
                .sorted(java.util.Comparator.comparing(User::getLastName)
                        .thenComparing(User::getFirstName))
                .collect(Collectors.toList());

        return Paginator.selectFromList(list, "User Directory",
                u -> String.format("%-10s %-25s %s", u.getId(), u.getFullName(), u.getRole()),
                scanner);
    }

    /**
     * Legacy print-only version — kept for internal use where no selection is needed.
     */
    public static void showUserDirectory(Collection<User> users, String excludeId) {
        System.out.println("\n  ── User Directory ──");
        System.out.printf("  %-10s %-25s %-12s%n", "ID", "Name", "Role");
        System.out.println("  " + "-".repeat(50));
        users.stream()
             .filter(u -> !u.getId().equals(excludeId))
             .sorted(java.util.Comparator.comparing(User::getLastName))
             .forEach(u -> System.out.printf("  %-10s %-25s %-12s%n",
                     u.getId(), u.getFullName(), u.getRole()));
    }
}
