package UniSpace.ui;

import UniSpace.enums.Faculty;
import UniSpace.exception.AuthenticationException;
import UniSpace.exception.ValidationException;
import UniSpace.model.user.Admin;
import UniSpace.model.user.Manager;
import UniSpace.model.user.Student;
import UniSpace.model.user.Teacher;
import UniSpace.model.user.User;
import UniSpace.patterns.UserFactory;
import UniSpace.service.AuthService;
import UniSpace.service.CourseService;
import UniSpace.service.MarkService;
import UniSpace.service.MessageService;
import UniSpace.service.NewsService;
import UniSpace.service.RegistrationService;
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
            System.out.println("  2. Register (new student)");
            System.out.println("  0. Exit");
            System.out.print("\n  Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> attemptLogin();
                case "2" -> register();
                case "0" -> {
                    System.out.println("\n  Goodbye!");
                    return;
                }
                default -> System.out.println("\n  Invalid option. Try again.");
            }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void register() {
        System.out.println("\n  ── Student Registration ──");

        System.out.print("  First name : ");
        String firstName = scanner.nextLine().trim();

        System.out.print("  Last name  : ");
        String lastName = scanner.nextLine().trim();

        System.out.print("  Email      : ");
        String email = scanner.nextLine().trim();

        System.out.print("  Password   : ");
        String password = scanner.nextLine().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            System.out.println("  [ERROR] All fields are required.");
            return;
        }

        // Pick faculty
        Faculty[] faculties = Faculty.values();
        System.out.println("\n  Select faculty:");
        for (int i = 0; i < faculties.length; i++)
            System.out.printf("  %d. %s%n", i + 1, faculties[i]);
        System.out.print("  Choice: ");
        Faculty faculty;
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= faculties.length) { System.out.println("  [ERROR] Invalid faculty."); return; }
            faculty = faculties[idx];
        } catch (NumberFormatException e) {
            System.out.println("  [ERROR] Invalid input.");
            return;
        }

        // Pick year
        System.out.print("  Year (1–4): ");
        int year;
        try {
            year = Integer.parseInt(scanner.nextLine().trim());
            if (year < 1 || year > 4) { System.out.println("  [ERROR] Year must be 1–4."); return; }
        } catch (NumberFormatException e) {
            System.out.println("  [ERROR] Invalid year.");
            return;
        }

        try {
            Student student = UserFactory.createStudent(firstName, lastName, email, password, year, faculty);
            authService.registerUser(student);
            System.out.println("\n  Registration successful!");
            System.out.println("  Welcome, " + student.getFullName() + "! Your ID: " + student.getId());
            System.out.println("  You can now login with your email and password.");
        } catch (ValidationException e) {
            System.out.println("  [ERROR] " + e.getMessage());
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
        CourseService       courseService       = CourseService.getInstance();
        MarkService         markService         = MarkService.getInstance();
        ResearchService     researchService     = ResearchService.getInstance();
        MessageService      messageService      = MessageService.getInstance();
        RegistrationService registrationService = RegistrationService.getInstance();
        NewsService         newsService         = NewsService.getInstance();

        if (user instanceof Student s) {
            new StudentMenu(s, courseService, markService, researchService, messageService).show();

        } else if (user instanceof Teacher t) {
            new TeacherMenu(t, courseService, markService, researchService, messageService).show();

        } else if (user instanceof Admin a) {
            new AdminMenu(a).show();

        } else if (user instanceof Manager m) {
            new ManagerMenu(m, courseService, markService,
                    registrationService, newsService, messageService).show();
        }
    }

    private void printHeader() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════╗");
        System.out.println("  ║      UNISPACE — Welcome              ║");
        System.out.println("  ╚══════════════════════════════════════╝");
    }
}
