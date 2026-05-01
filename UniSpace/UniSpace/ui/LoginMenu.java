package UniSpace.ui;

import UniSpace.exception.AuthenticationException;
import UniSpace.model.user.User;
import UniSpace.service.AuthService;

import java.util.Scanner;

public class LoginMenu {

    private final Scanner scanner;
    private final AuthService authService;

    public LoginMenu() {
        this.scanner = new Scanner(System.in);
        this.authService = AuthService.getInstance();
    }

    public void show() {
        while (true) {
            printHeader();
            System.out.println("  1. Login");
            System.out.println("  0. Exit");
            System.out.print("\n  Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> attemptLogin();
                case "0" -> {
                    System.out.println("\n  Goodbye!");
                    return;
                }
                default -> System.out.println("\n  Invalid option. Try again.");
            }
        }
    }

    private void attemptLogin() {
        System.out.println();
        System.out.print("  Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("  Password: ");
        String password = scanner.nextLine().trim();

        try {
            User user = authService.login(email, password);

            System.out.println("\n  Welcome, " + user.getFullName() + "! [" + user.getRole() + "]");
            System.out.println("  ----------------------------------------");

            // ПОЛИМОРФИЗМ: каждый тип пользователя показывает своё меню
            user.showMenu();

            // После выхода из меню — разлогинить
            authService.logout();

        } catch (AuthenticationException e) {
            System.out.println("\n  Login failed: " + e.getMessage());
        }
    }

    private void printHeader() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════╗");
        System.out.println("  ║      UNISPACE — Welcome              ║");
        System.out.println("  ╚══════════════════════════════════════╝");
    }
}