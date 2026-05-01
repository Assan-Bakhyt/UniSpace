package UniSpace.ui;

import UniSpace.exception.CourseRegistrationException;
import UniSpace.model.course.Course;
import UniSpace.model.course.Mark;
import UniSpace.service.CourseService;
import UniSpace.service.MarkService;

import java.util.List;
import java.util.Scanner;

/**
 * Console menu for students.
 * Handles: course registration, viewing marks, printing transcript.
 */
public class StudentMenu {

    private final String        studentId;
    private final CourseService courseService;
    private final MarkService   markService;
    private final Scanner       scanner;

    public StudentMenu(String studentId, CourseService courseService, MarkService markService) {
        this.studentId     = studentId;
        this.courseService = courseService;
        this.markService   = markService;
        this.scanner       = new Scanner(System.in);
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
                case "0" -> running = false;
                default  -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    // ── Menu actions ─────────────────────────────────────────────────────────

    private void registerCourse() {
        System.out.print("Enter course code to register: ");
        String code = scanner.nextLine().trim();
        try {
            courseService.registerStudentForCourse(studentId, code);
            System.out.println("Successfully registered for " + code);
        } catch (CourseRegistrationException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void dropCourse() {
        System.out.print("Enter course code to drop: ");
        String code = scanner.nextLine().trim();
        try {
            courseService.dropCourse(studentId, code);
            System.out.println("Dropped " + code);
        } catch (CourseRegistrationException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void viewMyCourses() {
        List<Course> courses = courseService.getStudentCourses(studentId);
        if (courses.isEmpty()) {
            System.out.println("You are not registered for any courses.");
            return;
        }
        System.out.println("\n── Your Courses ──");
        int total = 0;
        for (Course c : courses) {
            System.out.printf("  %-10s %-30s %d credits%n",
                    c.getCourseCode(), c.getName(), c.getCredits());
            total += c.getCredits();
        }
        System.out.println("  Total credits: " + total + " / 21");
    }

    private void viewMyMarks() {
        List<Mark> marks = markService.getMarksForStudent(studentId);
        if (marks.isEmpty()) {
            System.out.println("No marks recorded yet.");
            return;
        }
        System.out.println("\n── Your Marks ──");
        for (Mark m : marks) {
            System.out.println("  " + m);
        }
    }

    private void printTranscript() {
        System.out.println(markService.generateTranscript(studentId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void printMenu() {
        System.out.println("\n══════════════════════════════");
        System.out.println(" Student Menu — ID: " + studentId);
        System.out.println("══════════════════════════════");
        System.out.println(" 1. Register for a course");
        System.out.println(" 2. Drop a course");
        System.out.println(" 3. View my courses");
        System.out.println(" 4. View my marks");
        System.out.println(" 5. Print transcript");
        System.out.println(" 0. Exit");
        System.out.print("Choice: ");
    }
}
