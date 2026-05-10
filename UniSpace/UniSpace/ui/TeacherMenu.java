package UniSpace.ui;

import UniSpace.model.course.Course;
import UniSpace.model.course.Mark;
import UniSpace.model.user.Teacher;
import UniSpace.service.CourseService;
import UniSpace.service.MarkService;
import UniSpace.service.MessageService;
import UniSpace.service.ResearchService;

import java.util.List;
import java.util.Scanner;

/**
 * Console menu for teachers.
 */
public class TeacherMenu {

    private final Teacher         teacher;
    private final CourseService   courseService;
    private final MarkService     markService;
    private final ResearchService researchService;
    private final MessageService  messageService;
    private final Scanner         scanner;

    public TeacherMenu(Teacher teacher, CourseService courseService,
                       MarkService markService, ResearchService researchService,
                       MessageService messageService) {
        this.teacher         = teacher;
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
                case "1" -> viewMyCourses();
                case "2" -> putMark();
                case "3" -> viewCourseMarks();
                case "4" -> viewInbox();
                case "5" -> sendMessage();
                case "6" -> viewCourseStudents();
                case "7" -> {
                    if (teacher.isResearcher()) openResearcherMode();
                    else System.out.println("  You are not a researcher.");
                }
                case "0" -> running = false;
                default  -> System.out.println("  Invalid option. Try again.");
            }
        }
    }

    // ── Menu actions ──────────────────────────────────────────────────────────

    private void viewMyCourses() {
        List<Course> courses = courseService.getCoursesByInstructor(teacher.getId());
        if (courses.isEmpty()) { System.out.println("  No courses assigned to you."); return; }
        System.out.println("\n  ── Your Courses ──");
        for (Course c : courses) {
            System.out.printf("    %-10s %-30s (%d credits)%n",
                    c.getCourseCode(), c.getName(), c.getCredits());
        }
    }

    private void putMark() {
        String studentId = UniSpace.util.ConsoleHelper.readNonEmpty(scanner, "  Student ID: ");
        String courseCode = UniSpace.util.ConsoleHelper.readNonEmpty(scanner, "  Course code: ");

        if (courseService.getCoursesByInstructor(teacher.getId()).stream().noneMatch(c -> c.getCourseCode().equals(courseCode))) {
            System.out.println("  [ERROR] You are not teaching this course.");
            return;
        }

        String comp = UniSpace.util.ConsoleHelper.readNonEmpty(scanner, "  Component (1=att1 / 2=att2 / 3=final): ");
        double score = UniSpace.util.ConsoleHelper.readDouble(scanner, "  Score: ");


        try {
            switch (comp) {
                case "1" -> markService.setAtt1(studentId, courseCode, score);
                case "2" -> markService.setAtt2(studentId, courseCode, score);
                case "3" -> markService.setFinalExam(studentId, courseCode, score);
                default  -> { System.out.println("  [ERROR] Unknown component."); return; }
            }
            System.out.println("  Mark saved.");
            markService.getMark(studentId, courseCode).ifPresent(m ->
                    System.out.println("  Status: " + m));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void viewCourseMarks() {
        System.out.print("  Course code: ");
        String courseCode = scanner.nextLine().trim();
        System.out.println(markService.generateMarkReport(courseCode));
    }

    private void viewInbox() {
        List<String> messages = messageService.getInbox(teacher.getId());
        if (messages.isEmpty()) { System.out.println("  Inbox is empty."); return; }
        System.out.println("\n  ── Inbox ──");
        messages.forEach(m -> System.out.println("  " + m));
    }

    private void sendMessage() {
        System.out.print("  Recipient user ID: ");
        String toId = scanner.nextLine().trim();
        System.out.print("  Message: ");
        String text = scanner.nextLine().trim();
        boolean sent = messageService.send(teacher.getId(), teacher.getFullName(), toId, text);
        System.out.println(sent ? "  Message sent." : "  [ERROR] Recipient not found or not online.");
    }

    private void openResearcherMode() {
        new ResearcherMenu(teacher.getFaculty(), researchService).show();
    }

    private void viewCourseStudents() {
        String courseCode = UniSpace.util.ConsoleHelper.readNonEmpty(scanner, "  Course code: ");
        if (courseService.getCoursesByInstructor(teacher.getId()).stream().noneMatch(c -> c.getCourseCode().equals(courseCode))) {
            System.out.println("  [ERROR] You are not teaching this course.");
            return;
        }

        java.util.List<String> rows = new java.util.ArrayList<>();
        for (UniSpace.model.user.User u : UniSpace.storage.DataRepository.getInstance().getUsers().values()) {
            if (u instanceof UniSpace.model.user.Student s) {
                if (courseService.getStudentCourses(s.getId()).stream().anyMatch(c -> c.getCourseCode().equals(courseCode))) {
                    rows.add(String.format("%-10s %s", s.getId(), s.getFullName()));
                }
            }
        }
        UniSpace.util.ConsoleHelper.printTable("Students in " + courseCode, rows);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void printMenu() {
        boolean isRes = teacher.isResearcher();
        System.out.println("\n  ══════════════════════════════════════");
        System.out.println("   Teacher Menu — " + teacher.getFullName());
        System.out.println("  ══════════════════════════════════════");
        System.out.println("   1. View my courses");
        System.out.println("   2. Enter / update a mark");
        System.out.println("   3. View all marks for a course");
        System.out.println("   4. View inbox");
        System.out.println("   5. Send message");
        System.out.println("   6. View students in a course");
        System.out.println("   7. Researcher mode" + (isRes ? "" : " [not available]"));
        System.out.println("   0. Exit");
        System.out.print("  Choice: ");
    }
}
