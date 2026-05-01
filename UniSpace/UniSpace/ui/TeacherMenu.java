package UniSpace.ui;

import UniSpace.model.course.Course;
import UniSpace.model.course.Mark;
import UniSpace.service.CourseService;
import UniSpace.service.MarkService;

import java.util.List;
import java.util.Scanner;

/**
 * Console menu for teachers.
 * Handles: viewing assigned courses, entering/updating student marks.
 */
public class TeacherMenu {

    private final String        teacherId;
    private final CourseService courseService;
    private final MarkService   markService;
    private final Scanner       scanner;

    public TeacherMenu(String teacherId, CourseService courseService, MarkService markService) {
        this.teacherId     = teacherId;
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
                case "1" -> viewMyCourses();
                case "2" -> putMark();
                case "3" -> viewCourseMarks();
                case "0" -> running = false;
                default  -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    // ── Menu actions ─────────────────────────────────────────────────────────

    private void viewMyCourses() {
        List<Course> courses = courseService.getCoursesByInstructor(teacherId);
        if (courses.isEmpty()) {
            System.out.println("No courses assigned to you.");
            return;
        }
        System.out.println("\n── Your Courses ──");
        for (Course c : courses) {
            System.out.printf("  %-10s %-30s (%d credits)%n",
                    c.getCourseCode(), c.getName(), c.getCredits());
        }
    }

    private void putMark() {
        System.out.print("Student ID: ");
        String studentId = scanner.nextLine().trim();
        System.out.print("Course code: ");
        String courseCode = scanner.nextLine().trim();
        System.out.print("Component (1=att1 / 2=att2 / 3=final): ");
        String comp = scanner.nextLine().trim();

        System.out.print("Score: ");
        double score;
        try {
            score = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Invalid score.");
            return;
        }

        try {
            switch (comp) {
                case "1" -> markService.setAtt1(studentId, courseCode, score);
                case "2" -> markService.setAtt2(studentId, courseCode, score);
                case "3" -> markService.setFinalExam(studentId, courseCode, score);
                default  -> { System.out.println("[ERROR] Unknown component."); return; }
            }
            System.out.println("Mark saved successfully.");
            markService.getMark(studentId, courseCode).ifPresent(m ->
                    System.out.println("  Current status: " + m));
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void viewCourseMarks() {
        System.out.print("Course code: ");
        String courseCode = scanner.nextLine().trim();
        System.out.println(markService.generateMarkReport(courseCode));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void printMenu() {
        System.out.println("\n══════════════════════════════");
        System.out.println(" Teacher Menu — ID: " + teacherId);
        System.out.println("══════════════════════════════");
        System.out.println(" 1. View my courses");
        System.out.println(" 2. Enter/update a mark");
        System.out.println(" 3. View all marks for a course");
        System.out.println(" 0. Exit");
        System.out.print("Choice: ");
    }
}
