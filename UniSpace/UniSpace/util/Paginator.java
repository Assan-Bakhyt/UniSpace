package UniSpace.util;

import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generic paginated list display and selection utility.
 * Provides reusable list navigation across all menus.
 * Pattern: Utility (static methods).
 */
public class Paginator {

    public static final int PAGE_SIZE = 10;

    /**
     * Shows a paginated list and lets the user select one item.
     * Supports search/filter and page navigation.
     *
     * Commands: number = select, N = next page, P = prev page, S = search, 0 = back
     *
     * @return the selected item, or null if the user chose 0 (back/cancel)
     */
    public static <T> T selectFromList(List<T> items, String title,
                                        Function<T, String> display, Scanner scanner) {
        if (items == null || items.isEmpty()) {
            System.out.println(Colors.purple("  ── " + title + " ──"));
            System.out.println(Colors.gray("  (No items available)"));
            ConsoleHelper.pressEnterToContinue(scanner);
            return null;
        }

        String filter = "";
        int page = 0;

        while (true) {
            final String flt = filter;
            List<T> filtered = flt.isEmpty() ? items
                    : items.stream()
                           .filter(x -> display.apply(x).toLowerCase().contains(flt.toLowerCase()))
                           .collect(Collectors.toList());

            int totalPages = Math.max(1, (filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            if (page >= totalPages) page = Math.max(0, totalPages - 1);

            int from = page * PAGE_SIZE;
            int to   = Math.min(from + PAGE_SIZE, filtered.size());

            System.out.println(Colors.purple("\n  -- " + title
                    + (filter.isEmpty() ? "" : " [filter: \"" + filter + "\"]") + " --")
                    + Colors.gray(" (" + filtered.size() + " items, page " + (page + 1) + "/" + totalPages + ")"));

            if (filtered.isEmpty()) {
                System.out.println("  (No results for \"" + filter + "\")");
            } else {
                for (int i = from; i < to; i++) {
                    System.out.printf("  %s%2d.%s %s%n",
                            Colors.GRAY, i + 1, Colors.RESET, display.apply(filtered.get(i)));
                }
            }

            StringBuilder hint = new StringBuilder("\n  [#]=select");
            if (page < totalPages - 1) hint.append(", N=next");
            if (page > 0)              hint.append(", P=prev");
            hint.append(", S=search, 0=back");
            System.out.println(Colors.gray(hint.toString()));
            System.out.print(Colors.gray("  > "));

            String input = ConsoleHelper.readChoice(scanner);
            if (input.isEmpty()) continue;
            String lower = input.toLowerCase();

            if ("0".equals(input))                              return null;
            if ("n".equals(lower) && page < totalPages - 1)    { page++; continue; }
            if ("p".equals(lower) && page > 0)                 { page--; continue; }
            if ("s".equals(lower)) {
                System.out.print(Colors.gray("  Search (Enter to clear filter): "));
                filter = scanner.nextLine().trim();
                page = 0;
                continue;
            }

            try {
                int idx = Integer.parseInt(input) - 1;
                if (idx >= 0 && idx < filtered.size()) return filtered.get(idx);
            } catch (NumberFormatException ignored) {}

            System.out.println(Colors.red("  [ERROR] Invalid input — enter a number, N, P, S, or 0."));
        }
    }

    /**
     * Shows a paginated list for viewing only (no item selection).
     * Press 0 or Enter on last page to exit.
     *
     * Commands: N = next page, P = prev page, 0 = back
     */
    public static <T> void viewList(List<T> items, String title,
                                     Function<T, String> display, Scanner scanner) {
        if (items == null || items.isEmpty()) {
            System.out.println(Colors.purple("\n  ── " + title + " ──"));
            System.out.println(Colors.gray("  (No items found)"));
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }

        int page = 0;
        int totalPages = Math.max(1, (items.size() + PAGE_SIZE - 1) / PAGE_SIZE);

        while (true) {
            int from = page * PAGE_SIZE;
            int to   = Math.min(from + PAGE_SIZE, items.size());

            System.out.println(Colors.purple("\n  -- " + title + " --")
                    + Colors.gray(" (" + items.size() + " total, page " + (page + 1) + "/" + totalPages + ")"));
            for (int i = from; i < to; i++) {
                System.out.println("  " + display.apply(items.get(i)));
            }
            System.out.println();
            if (page > 0)              System.out.println(Colors.gray("  P. Previous page"));
            if (page < totalPages - 1) System.out.println(Colors.gray("  N. Next page"));
            System.out.println(Colors.gray("  0. Back"));
            System.out.print(Colors.gray("  > "));

            String input = scanner.nextLine().trim().toLowerCase();
            if ("0".equals(input) || input.isEmpty())        return;
            if ("n".equals(input) && page < totalPages - 1) { page++; continue; }
            if ("p".equals(input) && page > 0)              { page--; continue; }
        }
    }
}
