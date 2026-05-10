package UniSpace.ui;

import UniSpace.exception.CourseRegistrationException;
import UniSpace.exception.ValidationException;
import UniSpace.model.course.Course;
import UniSpace.model.course.Mark;
import UniSpace.model.user.Student;
import UniSpace.model.user.Teacher;
import UniSpace.model.user.User;
import UniSpace.model.course.RegistrationRequest;
import UniSpace.service.AuthService;
import UniSpace.service.CourseService;
import UniSpace.service.MarkService;
import UniSpace.service.MessageService;
import UniSpace.service.RegistrationService;
import UniSpace.service.ResearchService;
import UniSpace.storage.DataRepository;

import java.util.List;
import java.util.Scanner;

/**
 * Console menu for students.
 */
public class StudentMenu {

    private final Student         student;
    private final CourseService   courseService;
    private final MarkService     markService;
    private final ResearchService researchService;
    private final MessageService  messageService;
    private final Scanner         scanner;

    public StudentMenu(Student student, CourseService courseService,
                       MarkService markService, ResearchService researchService,
                       MessageService messageService) {
        this.student         = student;
        this.courseService   = courseService;
        this.markService     = markService;
        this.researchService = researchService;
        this.messageService  = messageService;
        this.scanner         = new Scanner(System.in);
    }

    public void show() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> registerCourse();
                case "2" -> dropCourse();
                case "3" -> viewMyCourses();
                case "4" -> viewMyMarks();
                case "5" -> printTranscript();
                case "6" -> rateTeacher();
                case "7" -> viewInbox();
                case "8" -> sendMessage();
                case "9" -> viewTeacherInfo();
                case "10" -> {
                    if (!student.isResearcher()) activateResearcherRole();
                    if (student.isResearcher()) openResearcherMode();
                }
                case "0" -> running = false;
                default  -> System.out.println("  Invalid option. Try again.");
            }
        }
    }

    // ── Menu actions ──────────────────────────────────────────────────────────

    private void registerCourse() {
        System.out.print("  Course code: ");
        String code = scanner.nextLine().trim();
        try {
            RegistrationRequest req = RegistrationService.getInstance()
                    .submitRequest(student.getId(), code);
            DataRepository.getInstance().save();
            System.out.println("  Request submitted — awaiting manager approval.");
            System.out.println("  Request ID: " + req.getRequestId().substring(0, 8) + "...");
        } catch (CourseRegistrationException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void dropCourse() {
        System.out.print("  Course code: ");
        String code = scanner.nextLine().trim();
        try {
            courseService.dropCourse(student.getId(), code);
            System.out.println("  Dropped " + code);
        } catch (CourseRegistrationException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void viewMyCourses() {
        List<Course> courses = courseService.getStudentCourses(student.getId());
        if (courses.isEmpty()) { System.out.println("  No courses registered."); return; }
        System.out.println("\n  ── Your Courses ──");
        int total = 0;
        for (Course c : courses) {
            System.out.printf("    %-10s %-30s %d credits%n",
                    c.getCourseCode(), c.getName(), c.getCredits());
            total += c.getCredits();
        }
        System.out.println("  Total credits: " + total + " / " + Student.MAX_CREDITS);
    }

    private void viewMyMarks() {
        List<Mark> marks = markService.getMarksForStudent(student.getId());
        if (marks.isEmpty()) { System.out.println("  No marks recorded yet."); return; }
        System.out.println("\n  ── Your Marks ──");
        for (Mark m : marks) System.out.println("    " + m);
    }

    private void printTranscript() {
        System.out.println(markService.generateTranscript(student.getId()));
    }

    private void rateTeacher() {
        String courseCode = UniSpace.util.ConsoleHelper.readNonEmpty(scanner, "  Course code: ");
        if (courseService.getStudentCourses(student.getId()).stream().noneMatch(c -> c.getCourseCode().equals(courseCode))) {
            System.out.println("  [ERROR] You are not registered for this course.");
            return;
        }

        String teacherId = UniSpace.util.ConsoleHelper.readNonEmpty(scanner, "  Teacher ID: ");
        
        Course course = courseService.findCourse(courseCode).orElse(null);
        if (course == null || !course.hasInstructor(teacherId)) {
            System.out.println("  [ERROR] Teacher is not an instructor for this course.");
            return;
        }

        double rating = UniSpace.util.ConsoleHelper.readDouble(scanner, "  Rating (1–5): ");
        User u = AuthService.getInstance().getUserById(teacherId);
        if (u instanceof Teacher teacher) {
            try {
                teacher.addRating(rating);
                DataRepository.getInstance().save();
                System.out.printf("  Rating %.1f submitted for %s.%n", rating, teacher.getFullName());
            } catch (ValidationException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        } else {
            System.out.println("  [ERROR] Teacher not found.");
        }
    }

    private void viewInbox() {
        List<String> messages = messageService.getInbox(student.getId());
        if (messages.isEmpty()) { System.out.println("  Inbox is empty."); return; }
        System.out.println("\n  ── Inbox ──");
        messages.forEach(m -> System.out.println("  " + m));
    }

    private void sendMessage() {
        System.out.print("  Recipient user ID: ");
        String toId = scanner.nextLine().trim();
        System.out.print("  Message: ");
        String text = scanner.nextLine().trim();
        boolean sent = messageService.send(student.getId(), student.getFullName(), toId, text);
        System.out.println(sent ? "  Message sent." : "  [ERROR] Recipient not found or not online.");
    }

    private void activateResearcherRole() {
        System.out.print("  Activate researcher role? (y/n): ");
        if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) return;
        student.activateResearcher();
        researchService.addResearcher(student.getResearcherProfile());
        DataRepository.getInstance().save();
        System.out.println("  Researcher role activated.");
    }

    private void openResearcherMode() {
        new ResearcherMenu(student.getResearcherProfile(), student.getFaculty(), researchService).show();
    }

    private void viewTeacherInfo() {
        String courseCode = UniSpace.util.ConsoleHelper.readNonEmpty(scanner, "  Course code: ");
        if (courseService.getStudentCourses(student.getId()).stream().noneMatch(c -> c.getCourseCode().equals(courseCode))) {
            System.out.println("  [ERROR] You are not registered for this course.");
            return;
        }
        
        Course course = courseService.findCourse(courseCode).orElse(null);
        if (course == null) return;
        
        java.util.List<String> rows = new java.util.ArrayList<>();
        for (String tid : course.getInstructors()) {
            User u = AuthService.getInstance().getUserById(tid);
            if (u instanceof Teacher t) {
                rows.add(String.format("%-10s %-20s %s (Rating: %.1f)", t.getId(), t.getFullName(), t.getTitle(), t.getRating()));
            }
        }
        UniSpace.util.ConsoleHelper.printTable("Instructors for " + courseCode, rows);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void printMenu() {
        boolean isRes = student.isResearcher();
        System.out.println("\n  ══════════════════════════════════════");
        System.out.println("   Student Menu — " + student.getFullName());
        System.out.println("  ══════════════════════════════════════");
        System.out.println("   1. Register for a course");
        System.out.println("   2. Drop a course");
        System.out.println("   3. View my courses");
        System.out.println("   4. View my marks");
        System.out.println("   5. Print transcript");
        System.out.println("   6. Rate a teacher");
        System.out.println("   7. View inbox");
        System.out.println("   8. Send message");
        System.out.println("   9. View teacher info for a course");
        System.out.println("   10. Researcher mode" + (isRes ? "" : " [not available]"));
        System.out.println("   0. Exit");
        System.out.print("  Choice: ");
    }
}
