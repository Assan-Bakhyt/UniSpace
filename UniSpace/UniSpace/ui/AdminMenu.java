package UniSpace.ui;

import UniSpace.model.user.Admin;
import UniSpace.model.user.User;
import UniSpace.model.user.Student;
import UniSpace.model.user.Teacher;
import UniSpace.model.user.Manager;
import UniSpace.service.AuthService;
import UniSpace.service.LogService;
import UniSpace.service.ComplaintService;
import UniSpace.model.log.LogEntry;
import UniSpace.model.complaint.Complaint;
import UniSpace.enums.UserRole;
import UniSpace.enums.Faculty;
import UniSpace.enums.TeacherTitle;
import UniSpace.enums.ManagerType;
import UniSpace.util.Validator;

import java.util.Scanner;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Menu interface for Admin users.
 * Provides full administration capabilities including:
 * - User management (Add, Remove, Update)
 * - Viewing system logs (sorted)
 * - Managing complaints (sorted)
 * Implements logging for all admin actions.
 *
 * Design Patterns used:
 * - Singleton (via AuthService, LogService, ComplaintService)
 * - Facade (AdminMenu acts as a facade for complex subsystem operations)
 */
public class AdminMenu {

    private final Admin admin;
    private final Scanner scanner = new Scanner(System.in);
    private final AuthService authService;
    private final LogService logService;
    private final ComplaintService complaintService;

    /**
     * Creates a new AdminMenu for the specified admin.
     *
     * @param admin the admin user who will use this menu
     */
    public AdminMenu(Admin admin) {
        this.admin = admin;
        this.authService = AuthService.getInstance();
        this.logService = LogService.getInstance();
        this.complaintService = ComplaintService.getInstance();
    }

    /**
     * Displays the admin menu and handles user interaction.
     * Main entry point for admin functionality.
     */
    public void show() {
        System.out.println("\n=== ADMIN MENU — " + admin.getFullName() + " ===");

        boolean running = true;
        while (running) {
            printMenuOptions();
            System.out.print("Select option (0-6): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addUser();
                    break;
                case "2":
                    removeUser();
                    break;
                case "3":
                    updateUser();
                    break;
                case "4":
                    viewLogs();
                    break;
                case "5":
                    viewComplaints();
                    break;
                case "6":
                    manageComplaints();
                    break;
                case "0":
                    System.out.println("Exiting Admin Menu...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Prints all available menu options for the admin.
     */
    private void printMenuOptions() {
        System.out.println("\n--- Admin Options ---");
        System.out.println("1. Add User");
        System.out.println("2. Remove User (by ID)");
        System.out.println("3. Update User (by ID)");
        System.out.println("4. View Logs (Sorted)");
        System.out.println("5. View Complaints (Sorted)");
        System.out.println("6. Manage Complaints");
        System.out.println("0. Exit");
    }

    /**
     * Adds a new user to the system.
     * Prompts for user details and creates the appropriate user type.
     * Validates email and password using Validator.
     * Allows selecting Faculty and year/course level.
     * Logs the action after completion.
     */
    private void addUser() {
        System.out.println("\n=== ADD USER ===");

        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        // Validate email
        if (!Validator.validateEmail(email)) {
            System.out.println("Invalid email format. Please use a valid email address.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        // Validate password
        if (!Validator.validatePassword(password)) {
            System.out.println("Invalid password. Password must be at least 6 characters long.");
            return;
        }

        System.out.println("Select role:");
        System.out.println("1. STUDENT");
        System.out.println("2. TEACHER");
        System.out.println("3. MANAGER");
        System.out.println("4. ADMIN");
        System.out.print("Choice (1-4): ");
        String roleChoice = scanner.nextLine().trim();

        UserRole role;
        switch (roleChoice) {
            case "1":
                role = UserRole.STUDENT;
                break;
            case "2":
                role = UserRole.TEACHER;
                break;
            case "3":
                role = UserRole.MANAGER;
                break;
            case "4":
                role = UserRole.ADMIN;
                break;
            default:
                System.out.println("Invalid role selected.");
                return;
        }

        // Select Faculty
        System.out.println("\nSelect Faculty:");
        System.out.println("1. FIT (Faculty of Information Technology)");
        System.out.println("2. CS (Computer Science)");
        System.out.println("3. MATH (Mathematics)");
        System.out.println("4. PHYSICS (Physics)");
        System.out.println("5. BS (Business School)");
        System.out.println("6. KMA (Kazakh Maritime Academy)");
        System.out.println("7. ISE (International School of Economics)");
        System.out.print("Choice (1-7): ");
        String facultyChoice = scanner.nextLine().trim();

        Faculty faculty;
        switch (facultyChoice) {
            case "1":
                faculty = Faculty.FIT;
                break;
            case "2":
                faculty = Faculty.CS;
                break;
            case "3":
                faculty = Faculty.MATH;
                break;
            case "4":
                faculty = Faculty.PHYSICS;
                break;
            case "5":
                faculty = Faculty.BS;
                break;
            case "6":
                faculty = Faculty.KMA;
                break;
            case "7":
                faculty = Faculty.ISE;
                break;
            default:
                System.out.println("Invalid faculty selected. Defaulting to FIT.");
                faculty = Faculty.FIT;
        }

        // Generate user ID
        String userId = "U-" + System.currentTimeMillis();

        // Create user using factory or directly based on role
        User newUser;
        switch (role) {
            case STUDENT:
                System.out.print("Enter year of study (1-4): ");
                int year = 1;
                try {
                    year = Integer.parseInt(scanner.nextLine().trim());
                    if (year < 1 || year > 4) {
                        year = 1;
                        System.out.println("Invalid year. Defaulting to 1.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Defaulting to year 1.");
                }
                newUser = new Student(userId, firstName, lastName, email, password, year, faculty);
                break;
            case TEACHER:
                System.out.println("Select Teacher Title:");
                System.out.println("1. TUTOR");
                System.out.println("2. SENIOR_LECTOR");
                System.out.println("3. LECTOR");
                System.out.println("4. PROFESSOR");
                System.out.print("Choice (1-4): ");
                String titleChoice = scanner.nextLine().trim();

                TeacherTitle title;
                switch (titleChoice) {
                    case "1":
                        title = TeacherTitle.TUTOR;
                        break;
                    case "2":
                        title = TeacherTitle.SENIOR_LECTOR;
                        break;
                    case "3":
                        title = TeacherTitle.LECTOR;
                        break;
                    case "4":
                        title = TeacherTitle.PROFESSOR;
                        break;
                    default:
                        title = TeacherTitle.TUTOR;
                        System.out.println("Invalid title. Defaulting to TUTOR.");
                }
                newUser = new Teacher(userId, firstName, lastName, email, password, faculty, 0.0, title);
                break;
            case MANAGER:
                System.out.println("Select Manager Type:");
                System.out.println("1. DEPARTMENT");
                System.out.println("2. OR");
                System.out.println("3. DEAN");
                System.out.print("Choice (1-3): ");
                String managerTypeChoice = scanner.nextLine().trim();

                ManagerType managerType;
                switch (managerTypeChoice) {
                    case "1":
                        managerType = ManagerType.DEPARTMENT;
                        break;
                    case "2":
                        managerType = ManagerType.OR;
                        break;
                    case "3":
                        managerType = ManagerType.DEAN;
                        break;
                    default:
                        managerType = ManagerType.DEPARTMENT;
                        System.out.println("Invalid type. Defaulting to DEPARTMENT.");
                }
                newUser = new Manager(userId, firstName, lastName, email, password, faculty, 0.0, managerType);
                break;
            case ADMIN:
                newUser = new Admin(userId, firstName, lastName, email, password, faculty);
                break;
            default:
                System.out.println("Unsupported role.");
                return;
        }

        try {
            authService.registerUser(newUser);
            System.out.println("User added successfully: " + newUser.getFullName());

            // Log the action
            logService.log(admin.getId(), admin.getFullName(),
                    "Added user: " + newUser.getFullName() + " (" + role + ", ID: " + userId + ")");
        } catch (Exception e) {
            System.out.println("Error adding user: " + e.getMessage());
        }
    }

    /**
     * Removes a user from the system by their unique ID.
     * First displays a list of users to help identify the correct ID.
     * Prompts for confirmation before deletion.
     * Logs the action after completion.
     */
    private void removeUser() {
        System.out.println("\n=== REMOVE USER ===");

        // Show list of users to help admin find the ID
        System.out.println("Current users in the system:");
        Map<String, User> allUsers = authService.getAllUsers();
        if (allUsers.isEmpty()) {
            System.out.println("No users found in the system.");
            return;
        }

        // Display first 10 users as a preview (to avoid flooding console)
        printUserListPreview(allUsers);

        System.out.print("\nEnter User ID to remove: ");
        String userId = scanner.nextLine().trim();

        // Find user by ID using direct Map lookup (O(1) instead of O(n))
        User userToRemove = allUsers.get(userId);

        if (userToRemove == null) {
            System.out.println("User not found with ID: " + userId);
            return;
        }

        System.out.println("Found user: " + userToRemove.getFullName() + " (" + userToRemove.getRole() + ")");
        System.out.print("Are you sure you want to remove this user? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(confirm)) {
            try {
                // Remove using email internally (as AuthService currently works)
                authService.removeUser(userToRemove.getEmail());
                System.out.println("User removed successfully.");

                // Log the action with ID for better traceability
                logService.log(admin.getId(), admin.getFullName(),
                        "Removed user: " + userToRemove.getFullName() + " (ID: " + userId + ")");
            } catch (Exception e) {
                System.out.println("Error removing user: " + e.getMessage());
            }
        } else {
            System.out.println("User removal cancelled.");
        }
    }

    /**
     * Helper method to print a preview of users (first 10).
     * Avoids code duplication between removeUser and updateUser methods.
     *
     * @param allUsers map of all users to display
     */
    private void printUserListPreview(Map<String, User> allUsers) {
        int count = 0;
        for (User u : allUsers.values()) {
            System.out.printf("ID: %-15s | Name: %-20s | Email: %s%n",
                    u.getId(), u.getFullName(), u.getEmail());
            count++;
            if (count >= 10) {
                System.out.println("... (showing first 10 users only)");
                break;
            }
        }
    }

    /**
     * Updates an existing user's information.
     * Allows modifying first name, last name, email, and password.
     * Uses User ID for identification (more reliable than email).
     * Logs the action after completion.
     */
    private void updateUser() {
        System.out.println("\n=== UPDATE USER ===");

        // Show list of users to help admin find the ID
        System.out.println("Current users in the system:");
        Map<String, User> allUsers = authService.getAllUsers();
        if (allUsers.isEmpty()) {
            System.out.println("No users found in the system.");
            return;
        }

        // Display first 10 users as a preview
        printUserListPreview(allUsers);

        System.out.print("\nEnter User ID to update: ");
        String userId = scanner.nextLine().trim();

        // Find user by ID using direct Map lookup
        User userToUpdate = allUsers.get(userId);

        if (userToUpdate == null) {
            System.out.println("User not found with ID: " + userId);
            return;
        }

        System.out.println("\nCurrent user info: " + userToUpdate);

        System.out.print("Enter new first name (or press Enter to keep current): ");
        String firstNameInput = scanner.nextLine().trim();
        String firstName = firstNameInput.isEmpty() ? userToUpdate.getFirstName() : firstNameInput;

        System.out.print("Enter new last name (or press Enter to keep current): ");
        String lastNameInput = scanner.nextLine().trim();
        String lastName = lastNameInput.isEmpty() ? userToUpdate.getLastName() : lastNameInput;

        System.out.print("Enter new email (or press Enter to keep current): ");
        String emailInput = scanner.nextLine().trim();
        String newEmail = emailInput.isEmpty() ? userToUpdate.getEmail() : emailInput;

        // Validate new email if changed
        if (!newEmail.equals(userToUpdate.getEmail()) && !Validator.validateEmail(newEmail)) {
            System.out.println("Invalid email format. Update cancelled.");
            return;
        }

        System.out.print("Enter new password (or press Enter to keep current): ");
        String passwordInput = scanner.nextLine().trim();
        String password = passwordInput.isEmpty() ? userToUpdate.getPassword() : passwordInput;

        // Validate new password if changed
        if (!password.equals(userToUpdate.getPassword()) && !Validator.validatePassword(password)) {
            System.out.println("Invalid password. Password must be at least 6 characters long. Update cancelled.");
            return;
        }

        // Update user fields
        userToUpdate.setFirstName(firstName);
        userToUpdate.setLastName(lastName);

        // If email changed, update it
        if (!newEmail.equals(userToUpdate.getEmail())) {
            userToUpdate.setEmail(newEmail);
        }

        if (!password.equals(userToUpdate.getPassword())) {
            userToUpdate.setPassword(password);
        }

        try {
            authService.updateUser(userToUpdate);
            System.out.println("User updated successfully.");

            // Log the action with ID for better traceability
            logService.log(admin.getId(), admin.getFullName(),
                    "Updated user: " + userToUpdate.getFullName() + " (ID: " + userId + ")");
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
    }

    /**
     * Displays system logs to the admin.
     * Shows recent logs and allows viewing logs by user.
     * Uses Comparator to sort logs by timestamp.
     */
    private void viewLogs() {
        System.out.println("\n=== VIEW LOGS ===");

        System.out.println("1. View Recent Logs (last 20, sorted by date)");
        System.out.println("2. View All Logs (sorted by date)");
        System.out.println("3. View Logs by User ID (sorted by date)");
        System.out.print("Select option (1-3): ");
        String choice = scanner.nextLine().trim();

        List<LogEntry> logs;

        switch (choice) {
            case "1":
                logs = logService.getRecentLogs(20);
                // Sort by timestamp descending (newest first)
                logs.sort(Comparator.comparing(LogEntry::getTimestamp).reversed());
                System.out.println("\n--- Recent Logs (last 20, sorted by date) ---");
                break;
            case "2":
                logs = logService.getAllLogs();
                // Sort by timestamp descending (newest first)
                logs.sort(Comparator.comparing(LogEntry::getTimestamp).reversed());
                System.out.println("\n--- All Logs (sorted by date) ---");
                break;
            case "3":
                System.out.print("Enter User ID: ");
                String userId = scanner.nextLine().trim();
                logs = logService.getLogsByUserId(userId);
                // Sort by timestamp descending (newest first)
                logs.sort(Comparator.comparing(LogEntry::getTimestamp).reversed());
                System.out.println("\n--- Logs for User: " + userId + " (sorted by date) ---");
                break;
            default:
                System.out.println("Invalid option.");
                return;
        }

        if (logs.isEmpty()) {
            System.out.println("No logs found.");
        } else {
            for (LogEntry entry : logs) {
                System.out.println(entry);
            }
            System.out.println("Total logs displayed: " + logs.size());
        }

        // Log the action
        logService.log(admin.getId(), admin.getFullName(), "Viewed system logs");
    }

    /**
     * Displays all complaints in the system.
     * Shows complaint details including sender, recipient, text, and status.
     * Uses Comparator to sort complaints by creation date.
     */
    private void viewComplaints() {
        System.out.println("\n=== VIEW COMPLAINTS ===");

        System.out.println("1. View All Complaints (sorted by date)");
        System.out.println("2. View Pending Complaints (sorted by date)");
        System.out.println("3. View Complaints by Recipient (sorted by date)");
        System.out.print("Select option (1-3): ");
        String choice = scanner.nextLine().trim();

        List<Complaint> complaints;

        switch (choice) {
            case "1":
                complaints = (List<Complaint>) complaintService.getAllComplaints();
                // Sort by creation date descending (newest first)
                complaints.sort(Comparator.comparing(Complaint::getCreatedAt).reversed());
                System.out.println("\n--- All Complaints (sorted by date) ---");
                break;
            case "2":
                complaints = complaintService.getPendingComplaints();
                // Sort by creation date descending (newest first)
                complaints.sort(Comparator.comparing(Complaint::getCreatedAt).reversed());
                System.out.println("\n--- Pending Complaints (sorted by date) ---");
                break;
            case "3":
                System.out.print("Enter Recipient User ID: ");
                String recipientId = scanner.nextLine().trim();
                complaints = complaintService.getComplaintsByRecipientId(recipientId);
                // Sort by creation date descending (newest first)
                complaints.sort(Comparator.comparing(Complaint::getCreatedAt).reversed());
                System.out.println("\n--- Complaints against User: " + recipientId + " (sorted by date) ---");
                break;
            default:
                System.out.println("Invalid option.");
                return;
        }

        if (complaints.isEmpty()) {
            System.out.println("No complaints found.");
        } else {
            for (Complaint complaint : complaints) {
                System.out.println(complaint);
            }
            System.out.println("Total complaints displayed: " + complaints.size());
        }

        // Log the action
        logService.log(admin.getId(), admin.getFullName(), "Viewed complaints");
    }

    /**
     * Allows admin to manage (resolve/reject) complaints.
     * Shows pending complaints and allows status updates.
     */
    private void manageComplaints() {
        System.out.println("\n=== MANAGE COMPLAINTS ===");

        List<Complaint> pendingComplaints = complaintService.getPendingComplaints();

        if (pendingComplaints.isEmpty()) {
            System.out.println("No pending complaints to manage.");
            // Log the action
            logService.log(admin.getId(), admin.getFullName(), "Attempted to manage complaints - none pending");
            return;
        }

        System.out.println("Pending complaints:");
        for (int i = 0; i < pendingComplaints.size(); i++) {
            Complaint c = pendingComplaints.get(i);
            System.out.println((i + 1) + ". " + c.getId() + " | From: " + c.getSenderName() +
                    " To: " + c.getRecipientName());
        }

        System.out.print("\nEnter complaint number to manage (1-" + pendingComplaints.size() + ", or 0 to cancel): ");
        String choice = scanner.nextLine().trim();

        int complaintIndex;
        try {
            complaintIndex = Integer.parseInt(choice);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (complaintIndex == 0) {
            System.out.println("Cancelled.");
            return;
        }

        if (complaintIndex < 1 || complaintIndex > pendingComplaints.size()) {
            System.out.println("Invalid complaint number.");
            return;
        }

        Complaint selectedComplaint = pendingComplaints.get(complaintIndex - 1);
        System.out.println("\nSelected complaint:");
        System.out.println(selectedComplaint);

        System.out.println("\nActions:");
        System.out.println("1. Resolve Complaint");
        System.out.println("2. Reject Complaint");
        System.out.print("Select action (1-2): ");
        String actionChoice = scanner.nextLine().trim();

        boolean success;
        String action;

        switch (actionChoice) {
            case "1":
                success = complaintService.resolveComplaint(selectedComplaint.getId());
                action = "resolved";
                break;
            case "2":
                success = complaintService.rejectComplaint(selectedComplaint.getId());
                action = "rejected";
                break;
            default:
                System.out.println("Invalid action.");
                return;
        }

        if (success) {
            System.out.println("Complaint " + action + " successfully.");

            // Log the action
            logService.log(admin.getId(), admin.getFullName(),
                    action + " complaint: " + selectedComplaint.getId());
        } else {
            System.out.println("Failed to update complaint status.");
        }
    }
}