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
import UniSpace.service.MessageService;
import UniSpace.service.ResearchService;

import java.util.Scanner;

/**
 * Entry-point menu: handles login and dispatches to the appropriate role menu.
 */
public class LoginMenu {

    private final Scanner      scanner;
    private final AuthService  authService;

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

    // ── Private helpers ───────────────────────────────────────────────────────

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

            // Register user with the messaging system for this session
            MessageService.getInstance().register(user.getId(), user.getFullName());

            dispatchMenu(user);

            authService.logout();

        } catch (AuthenticationException e) {
            System.out.println("\n  Login failed: " + e.getMessage());
        }
    }

    /**
     * Routes the logged-in user to their primary menu.
     * All UI–service wiring lives here — model classes stay free of UI imports.
     */
    private void dispatchMenu(User user) {
        CourseService    courseService    = CourseService.getInstance();
        MarkService      markService      = MarkService.getInstance();
        ResearchService  researchService  = ResearchService.getInstance();
        MessageService   messageService   = MessageService.getInstance();

        if (user instanceof Student s) {
            new StudentMenu(s, courseService, markService, researchService, messageService).show();

        } else if (user instanceof Teacher t) {
            new TeacherMenu(t, courseService, markService, researchService, messageService).show();

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
