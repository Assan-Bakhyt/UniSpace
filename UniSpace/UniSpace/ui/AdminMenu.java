package UniSpace.ui;

import UniSpace.model.course.Mark;
import UniSpace.model.user.Admin;
import UniSpace.model.user.Manager;
import UniSpace.model.user.Student;
import UniSpace.model.user.Teacher;
import UniSpace.model.user.User;
import UniSpace.service.AuthService;
import UniSpace.service.CourseService;
import UniSpace.service.LogService;
import UniSpace.service.MarkService;
import UniSpace.service.ComplaintService;
import UniSpace.model.log.LogEntry;
import UniSpace.model.complaint.Complaint;
import UniSpace.enums.UserRole;
import UniSpace.enums.Faculty;
import UniSpace.enums.TeacherTitle;
import UniSpace.enums.ManagerType;
import UniSpace.storage.DataRepository;
import UniSpace.util.Colors;
import UniSpace.util.ConsoleHelper;
import UniSpace.util.Paginator;
import UniSpace.util.Validator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class AdminMenu {

    private final Admin admin;
    private final Scanner scanner = new Scanner(System.in);
    private final AuthService authService;
    private final LogService logService;
    private final ComplaintService complaintService;

    public AdminMenu(Admin admin) {
        this.admin = admin;
        this.authService = AuthService.getInstance();
        this.logService = LogService.getInstance();
        this.complaintService = ComplaintService.getInstance();
    }

    public void show() {
        boolean running = true;
        while (running) {
            printMenuOptions();
            String choice = ConsoleHelper.readChoice(scanner);
            switch (choice) {
                case "1" -> addUser();
                case "2" -> removeUser();
                case "3" -> updateUser();
                case "4" -> viewLogs();
                case "5" -> viewComplaints();
                case "6" -> manageComplaints();
                case "7" -> closeYear();
                case "0" -> running = false;
                default  -> System.out.println(Colors.red("  Invalid option. Try again."));
            }
        }
    }

    private void printMenuOptions() {
        System.out.println(Colors.gray("\n  ══════════════════════════════════════"));
        System.out.println(Colors.bold("   Admin Menu - " + admin.getFullName()));
        System.out.println(Colors.gray("  ══════════════════════════════════════"));
        System.out.println("   1. Add User");
        System.out.println("   2. Remove User");
        System.out.println("   3. Update User");
        System.out.println("   4. View Logs");
        System.out.println("   5. View Complaints");
        System.out.println("   6. Manage Complaints");
        System.out.println("   7. Close Academic Year");
        System.out.println("   0. Logout");
        System.out.print("  Choice: ");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  1. ADD USER
    // ══════════════════════════════════════════════════════════════════════════

    private void addUser() {
        System.out.println(Colors.purple("\n  ── Add User ──"));

        System.out.print("  First name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("  Last name: ");
        String lastName = scanner.nextLine().trim();
        System.out.print("  Email: ");
        String email = scanner.nextLine().trim();

        if (!Validator.validateEmail(email)) {
            System.out.println(Colors.red("  [ERROR] Invalid email format."));
            return;
        }

        System.out.print("  Password: ");
        String password = scanner.nextLine().trim();

        if (!Validator.validatePassword(password)) {
            System.out.println(Colors.red("  [ERROR] Password must be at least 6 characters."));
            return;
        }

        System.out.println("  Role: 1=STUDENT  2=TEACHER  3=MANAGER  4=ADMIN");
        System.out.print("  Choice: ");
        String roleChoice = scanner.nextLine().trim();

        UserRole role = switch (roleChoice) {
            case "1" -> UserRole.STUDENT;
            case "2" -> UserRole.TEACHER;
            case "3" -> UserRole.MANAGER;
            case "4" -> UserRole.ADMIN;
            default  -> null;
        };
        if (role == null) { System.out.println(Colors.red("  [ERROR] Invalid role.")); return; }

        Faculty faculty = pickFaculty();
        if (faculty == null) return;

        String userId = "U-" + System.currentTimeMillis();

        User newUser = switch (role) {
            case STUDENT -> {
                System.out.print("  Year of study (1–4): ");
                int year = 1;
                try { year = Integer.parseInt(scanner.nextLine().trim()); }
                catch (NumberFormatException ignored) {}
                if (year < 1 || year > 4) year = 1;
                yield new Student(userId, firstName, lastName, email, password, year, faculty);
            }
            case TEACHER -> {
                System.out.println("  Title: 1=TUTOR  2=SENIOR_LECTOR  3=LECTOR  4=PROFESSOR");
                System.out.print("  Choice: ");
                TeacherTitle title = switch (scanner.nextLine().trim()) {
                    case "2" -> TeacherTitle.SENIOR_LECTOR;
                    case "3" -> TeacherTitle.LECTOR;
                    case "4" -> TeacherTitle.PROFESSOR;
                    default  -> TeacherTitle.TUTOR;
                };
                yield new Teacher(userId, firstName, lastName, email, password, faculty, 0.0, title);
            }
            case MANAGER -> {
                System.out.println("  Type: 1=DEPARTMENT  2=OR  3=DEAN");
                System.out.print("  Choice: ");
                ManagerType mt = switch (scanner.nextLine().trim()) {
                    case "2" -> ManagerType.OR;
                    case "3" -> ManagerType.DEAN;
                    default  -> ManagerType.DEPARTMENT;
                };
                yield new Manager(userId, firstName, lastName, email, password, faculty, 0.0, mt);
            }
            case ADMIN -> new Admin(userId, firstName, lastName, email, password, faculty);
            default    -> null;
        };

        if (newUser == null) { System.out.println(Colors.red("  [ERROR] Unsupported role.")); return; }

        try {
            authService.registerUser(newUser);
            DataRepository.getInstance().save();
            System.out.println(Colors.green("  User added: " + newUser.getFullName() + " (ID: " + userId + ")"));
            logService.log(admin.getId(), admin.getFullName(),
                    "Added user: " + newUser.getFullName() + " (" + role + ")");
            DataRepository.getInstance().save();
        } catch (Exception e) {
            System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  2. REMOVE USER
    // ══════════════════════════════════════════════════════════════════════════

    private void removeUser() {
        System.out.println(Colors.purple("\n  ── Remove User ──"));

        List<User> users = getSortedUsers();
        User target = Paginator.selectFromList(users, "Select User to Remove",
                u -> String.format("%-10s %-25s %s", u.getId(), u.getFullName(), u.getRole()),
                scanner);
        if (target == null) return;

        System.out.println("  Selected: " + target.getFullName() + " (" + target.getRole() + ")");
        System.out.print(Colors.red("  Confirm removal? (yes/No): "));
        if (!"yes".equalsIgnoreCase(scanner.nextLine().trim())) {
            System.out.println(Colors.gray("  Cancelled."));
            return;
        }

        try {
            authService.removeUser(target.getEmail());
            DataRepository.getInstance().save();
            System.out.println(Colors.green("  User removed."));
            logService.log(admin.getId(), admin.getFullName(),
                    "Removed user: " + target.getFullName() + " (ID: " + target.getId() + ")");
            DataRepository.getInstance().save();
        } catch (Exception e) {
            System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  3. UPDATE USER
    // ══════════════════════════════════════════════════════════════════════════

    private void updateUser() {
        System.out.println(Colors.purple("\n  ── Update User ──"));

        List<User> users = getSortedUsers();
        User target = Paginator.selectFromList(users, "Select User to Update",
                u -> String.format("%-10s %-25s %s", u.getId(), u.getFullName(), u.getRole()),
                scanner);
        if (target == null) return;

        System.out.println("  Current: " + target);

        System.out.print("  New first name (Enter to keep): ");
        String fn = scanner.nextLine().trim();
        if (!fn.isEmpty()) target.setFirstName(fn);

        System.out.print("  New last name (Enter to keep): ");
        String ln = scanner.nextLine().trim();
        if (!ln.isEmpty()) target.setLastName(ln);

        System.out.print("  New email (Enter to keep): ");
        String em = scanner.nextLine().trim();
        if (!em.isEmpty()) {
            if (!Validator.validateEmail(em)) {
                System.out.println(Colors.red("  [ERROR] Invalid email. Update cancelled."));
                return;
            }
            target.setEmail(em);
        }

        System.out.print("  New password (Enter to keep): ");
        String pw = scanner.nextLine().trim();
        if (!pw.isEmpty()) {
            if (!Validator.validatePassword(pw)) {
                System.out.println(Colors.red("  [ERROR] Invalid password. Update cancelled."));
                return;
            }
            target.setPassword(Validator.hashPassword(pw));
        }

        try {
            authService.updateUser(target);
            DataRepository.getInstance().save();
            System.out.println(Colors.green("  User updated: " + target.getFullName()));
            logService.log(admin.getId(), admin.getFullName(),
                    "Updated user: " + target.getFullName() + " (ID: " + target.getId() + ")");
            DataRepository.getInstance().save();
        } catch (Exception e) {
            System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  4. VIEW LOGS
    // ══════════════════════════════════════════════════════════════════════════

    private void viewLogs() {
        boolean back = false;
        while (!back) {
            System.out.println(Colors.purple("\n  ── View Logs ──"));
            System.out.println("   1. Recent logs (last 20)");
            System.out.println("   2. All logs");
            System.out.println("   3. Logs by user");
            System.out.println(Colors.gray("   0. <- Back"));
            System.out.print("  Choice: ");

            switch (ConsoleHelper.readChoice(scanner)) {
                case "1" -> {
                    List<LogEntry> logs = new ArrayList<>(logService.getRecentLogs(20));
                    logs.sort(Comparator.comparing(LogEntry::getTimestamp).reversed());
                    Paginator.viewList(logs, "Recent Logs", LogEntry::toString, scanner);
                }
                case "2" -> {
                    List<LogEntry> logs = new ArrayList<>(logService.getAllLogs());
                    logs.sort(Comparator.comparing(LogEntry::getTimestamp).reversed());
                    Paginator.viewList(logs, "All Logs", LogEntry::toString, scanner);
                }
                case "3" -> {
                    System.out.print("  User ID: ");
                    String uid = ConsoleHelper.readChoice(scanner);
                    List<LogEntry> logs = new ArrayList<>(logService.getLogsByUserId(uid));
                    logs.sort(Comparator.comparing(LogEntry::getTimestamp).reversed());
                    Paginator.viewList(logs, "Logs for " + uid, LogEntry::toString, scanner);
                }
                case "0" -> back = true;
                default  -> System.out.println(Colors.red("  Invalid option."));
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  5. VIEW COMPLAINTS
    // ══════════════════════════════════════════════════════════════════════════

    private void viewComplaints() {
        boolean back = false;
        while (!back) {
            System.out.println(Colors.purple("\n  ── View Complaints ──"));
            System.out.println("   1. All complaints");
            System.out.println("   2. Pending complaints");
            System.out.println("   3. By recipient ID");
            System.out.println(Colors.gray("   0. <- Back"));
            System.out.print("  Choice: ");

            switch (ConsoleHelper.readChoice(scanner)) {
                case "1" -> {
                    List<Complaint> list = new ArrayList<>(complaintService.getAllComplaints());
                    list.sort(Comparator.comparing(Complaint::getCreatedAt).reversed());
                    Paginator.viewList(list, "All Complaints", AdminMenu::formatComplaint, scanner);
                }
                case "2" -> {
                    List<Complaint> list = new ArrayList<>(complaintService.getPendingComplaints());
                    list.sort(Comparator.comparing(Complaint::getCreatedAt).reversed());
                    Paginator.viewList(list, "Pending Complaints", AdminMenu::formatComplaint, scanner);
                }
                case "3" -> {
                    System.out.print("  Recipient user ID: ");
                    String rid = ConsoleHelper.readChoice(scanner);
                    List<Complaint> list = new ArrayList<>(
                            complaintService.getComplaintsByRecipientId(rid));
                    list.sort(Comparator.comparing(Complaint::getCreatedAt).reversed());
                    Paginator.viewList(list, "Complaints against " + rid, AdminMenu::formatComplaint, scanner);
                }
                case "0" -> back = true;
                default  -> System.out.println(Colors.red("  Invalid option."));
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  6. MANAGE COMPLAINTS
    // ══════════════════════════════════════════════════════════════════════════

    private void manageComplaints() {
        List<Complaint> pending = complaintService.getPendingComplaints();
        if (pending.isEmpty()) {
            System.out.println("  No pending complaints.");
            return;
        }

        Complaint selected = Paginator.selectFromList(pending, "Pending Complaints",
                c -> Colors.gray(String.format("[%s] #%s  From: %s  ->  %s",
                        c.getCreatedAt().toLocalDate(), c.getId().substring(0, 8),
                        c.getSenderName(), c.getRecipientName())),
                scanner);
        if (selected == null) return;

        System.out.println("  " + formatComplaint(selected));
        System.out.println();
        System.out.println(Colors.green("  1. Resolve") + "   " + Colors.red("  2. Reject") + "   " + Colors.gray("  0. Cancel"));
        System.out.print("  Choice: ");

        boolean success;
        String action;
        switch (ConsoleHelper.readChoice(scanner)) {
            case "1" -> { success = complaintService.resolveComplaint(selected.getId()); action = "resolved"; }
            case "2" -> { success = complaintService.rejectComplaint(selected.getId());  action = "rejected"; }
            default  -> { System.out.println(Colors.gray("  Cancelled.")); return; }
        }

        if (success) {
            DataRepository.getInstance().save();
            String colored = "resolved".equals(action) ? Colors.green("  Complaint resolved.") : Colors.red("  Complaint rejected.");
            System.out.println(colored);
            logService.log(admin.getId(), admin.getFullName(), action + " complaint: " + selected.getId());
            DataRepository.getInstance().save();
        } else {
            System.out.println(Colors.red("  Failed to update complaint status."));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  7. CLOSE ACADEMIC YEAR
    // ══════════════════════════════════════════════════════════════════════════

    private void closeYear() {
        System.out.println(Colors.purple("\n  ── Close Academic Year ──"));
        MarkService markService = MarkService.getInstance();

        List<Student> students = authService.getAllUsers().values().stream()
                .filter(u -> u instanceof Student).map(u -> (Student) u)
                .sorted(Comparator.comparing(User::getLastName))
                .collect(Collectors.toList());

        if (students.isEmpty()) { System.out.println("  No students in the system."); return; }

        System.out.println("\n  Students to process:");
        System.out.printf("  %-8s %-22s %-6s %-8s %s%n", "ID", "Name", "Year", "GPA", "Status");
        System.out.println("  " + "─".repeat(60));
        for (Student s : students) {
            List<Mark> marks = markService.getMarksForStudent(s.getId());
            boolean hasF  = marks.stream().anyMatch(m -> "F".equals(m.getLetterGrade()));
            boolean hasFX = marks.stream().anyMatch(m -> "FX".equals(m.getLetterGrade()));
            String status = hasF ? Colors.red("FAIL (holds year)") : hasFX ? Colors.red("FX (retake exam)") : Colors.green("PASS");
            System.out.printf("  %-8s %-22s %-6d %-8.2f %s%n",
                    s.getId(), s.getFullName(), s.getYear(),
                    markService.getGpa(s.getId()), status);
        }

        System.out.print(Colors.yellow("\n  Proceed with closing year? (yes/No): "));
        if (!"yes".equalsIgnoreCase(scanner.nextLine().trim())) {
            System.out.println(Colors.gray("  Cancelled."));
            return;
        }

        int advanced = 0, held = 0, graduated = 0;
        for (Student s : students) {
            boolean hasF = markService.getMarksForStudent(s.getId())
                    .stream().anyMatch(m -> "F".equals(m.getLetterGrade()));
            if (hasF) { held++; continue; }
            if (s.getYear() >= 4) {
                graduated++;
                logService.log(admin.getId(), admin.getFullName(),
                        "Graduated: " + s.getFullName() + " (ID: " + s.getId() + ")");
            } else {
                s.setYear(s.getYear() + 1);
                advanced++;
            }
        }

        DataRepository.getInstance().save();
        System.out.println(Colors.green("\n  Academic year closed."));
        System.out.printf("  Advanced: %d | " + Colors.green("Graduated: %d") + " | " + Colors.red("Held back (F): %d") + "%n",
                advanced, graduated, held);
        logService.log(admin.getId(), admin.getFullName(),
                "Closed year — advanced: " + advanced + ", grad: " + graduated + ", held: " + held);
        DataRepository.getInstance().save();
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String formatComplaint(Complaint c) {
        String statusStr = switch (c.getStatus()) {
            case PENDING      -> Colors.yellow("PENDING");
            case UNDER_REVIEW -> Colors.cyan("UNDER REVIEW");
            case RESOLVED     -> Colors.green("RESOLVED");
            case REJECTED     -> Colors.red("REJECTED");
        };
        return Colors.gray(String.format("[%s] #%s | From: %s  ->  %s | Status: ",
                c.getCreatedAt().toLocalDate(),
                c.getId().substring(0, 8),
                c.getSenderName(), c.getRecipientName()))
                + statusStr
                + Colors.gray("\n    \"" + c.getText() + "\"");
    }

    private List<User> getSortedUsers() {
        return authService.getAllUsers().values().stream()
                .sorted(Comparator.comparing(User::getLastName).thenComparing(User::getFirstName))
                .collect(Collectors.toList());
    }

    private Faculty pickFaculty() {
        Faculty[] values = Faculty.values();
        System.out.println("  Faculty:");
        for (int i = 0; i < values.length; i++)
            System.out.printf("  %d. %s%n", i + 1, values[i]);
        System.out.print("  Choice: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx >= 0 && idx < values.length) return values[idx];
        } catch (NumberFormatException ignored) {}
        System.out.println(Colors.red("  [ERROR] Invalid selection."));
        return null;
    }
}
