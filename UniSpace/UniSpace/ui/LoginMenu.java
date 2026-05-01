package UniSpace.ui;

import UniSpace.exception.AuthenticationException;
import UniSpace.model.user.Admin;
import UniSpace.model.user.Manager;
import UniSpace.model.user.Student;
import UniSpace.model.user.Teacher;
import UniSpace.model.user.User;
import UniSpace.service.AuthService;
import UniSpace.service.CourseService;
import UniSpace.service.MarkService;

import java.util.Scanner;

public class LoginMenu {

    private final Scanner     scanner;
    private final AuthService authService;

    public LoginMenu() {
        this.scanner     = new Scanner(System.in);
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

            dispatchMenu(user);

            authService.logout();

        } catch (AuthenticationException e) {
            System.out.println("\n  Login failed: " + e.getMessage());
        }
    }

    /**
     * Opens the appropriate menu based on the logged-in user's concrete type.
     * All UI–service wiring lives here, keeping model classes free of UI/service imports.
     */
    private void dispatchMenu(User user) {
        CourseService courseService = CourseService.getInstance();
        MarkService   markService   = MarkService.getInstance();

        if (user instanceof Student s) {
            new StudentMenu(s.getId(), courseService, markService).show();
        } else if (user instanceof Teacher t) {
            new TeacherMenu(t.getId(), courseService, markService).show();
        } else if (user instanceof Admin a) {
            new AdminMenu(a).show();
        } else if (user instanceof Manager m) {
            new ManagerMenu(m).show();
        }
    }

    private void printHeader() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════╗");
        System.out.println("  ║      UNISPACE — Welcome              ║");
        System.out.println("  ╚══════════════════════════════════════╝");
    }
}
