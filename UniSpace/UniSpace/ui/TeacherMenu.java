package UniSpace.ui;

import UniSpace.exception.ValidationException;
import UniSpace.model.course.Course;
import UniSpace.model.research.ResearcherRequest;
import UniSpace.model.user.Teacher;
import UniSpace.model.user.User;
import UniSpace.service.ComplaintService;
import UniSpace.service.LogService;
import UniSpace.service.CourseService;
import UniSpace.service.MarkService;
import UniSpace.service.MessageService;
import UniSpace.service.ResearchService;
import UniSpace.service.ResearcherRequestService;
import UniSpace.enums.TeacherTitle;
import UniSpace.enums.UserRole;
import UniSpace.service.AcademicYearService;
import UniSpace.storage.DataRepository;
import UniSpace.util.Colors;
import UniSpace.util.ConsoleHelper;

import java.util.List;

/**
 * Console menu for teachers.
 */
public class TeacherMenu extends BaseMenu {

    private final Teacher         teacher;
    private final CourseService   courseService;
    private final MarkService     markService;
    private final ResearchService researchService;

    public TeacherMenu(Teacher teacher, CourseService courseService,
                       MarkService markService, ResearchService researchService,
                       MessageService messageService) {
        super(messageService);
        this.teacher         = teacher;
        this.courseService   = courseService;
        this.markService     = markService;
        this.researchService = researchService;
    }

    @Override
    protected User currentUser() { return teacher; }

    public void show() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = ConsoleHelper.readChoice(scanner);
            switch (choice) {
                case "1" -> viewMyCourses();
                case "2" -> putMark();
                case "3" -> viewCourseMarks();
                case "4" -> viewCourseStudents();
                case "5" -> viewInbox();
                case "6" -> sendMessage();
                case "7" -> sendComplaint();
                case "8" -> handleResearcherOption();
                case "0" -> running = false;
                default  -> System.out.println(Colors.red("  Invalid option. Try again."));
            }
        }
    }

    // ── Menu actions ──────────────────────────────────────────────────────────

    private void viewMyCourses() {
        List<Course> courses = courseService.getCoursesByInstructor(teacher.getId());
        if (courses.isEmpty()) { System.out.println("  No courses assigned to you."); }
        else {
            System.out.println(Colors.purple("\n  -- Your Courses --"));
            System.out.printf("  %-10s %-32s %-8s %-12s %s%n", "Code", "Name", "Credits", "Faculty", "Year");
            System.out.println("  " + "-".repeat(72));
            for (Course c : courses) {
                String fac = c.getTargetFaculty() != null ? c.getTargetFaculty().toString() : "All";
                String yr  = c.getTargetYear() > 0 ? "Y" + c.getTargetYear() : "All";
                System.out.printf("  %-10s %-32s %-8d %-12s %s%n",
                        c.getCourseCode(), c.getName(), c.getCredits(), fac, yr);
            }
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void putMark() {
        if (AcademicYearService.getInstance().isYearClosed()) {
            System.out.println(Colors.red("\n  [LOCKED] The academic year is closed."
                    + " Marks cannot be entered or modified."));
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }
        List<Course> courses = courseService.getCoursesByInstructor(teacher.getId());
        if (courses.isEmpty()) { System.out.println("  No courses assigned to you."); return; }

        System.out.println(Colors.purple("\n  -- Enter Mark --"));
        System.out.println("  Your courses:");
        for (int i = 0; i < courses.size(); i++)
            System.out.printf("  %d. [%-8s] %s%n", i + 1, courses.get(i).getCourseCode(), courses.get(i).getName());

        int idx = ConsoleHelper.readInt(scanner, "  Select course: ") - 1;
        if (idx < 0 || idx >= courses.size()) { System.out.println("  Invalid selection."); return; }
        String courseCode = courses.get(idx).getCourseCode();

        // Show enrolled students
        List<String> studentRows = new java.util.ArrayList<>();
        for (User u : DataRepository.getInstance().getUsers().values()) {
            if (u instanceof UniSpace.model.user.Student s &&
                    courseService.getStudentCourses(s.getId()).stream()
                            .anyMatch(c -> c.getCourseCode().equals(courseCode))) {
                studentRows.add(String.format("  %-10s %s", s.getId(), s.getFullName()));
            }
        }
        if (studentRows.isEmpty()) {
            System.out.println(Colors.yellow("  No students enrolled in this course."));
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }
        System.out.println("  Enrolled students:");
        studentRows.forEach(System.out::println);

        String studentId = ConsoleHelper.readNonEmpty(scanner, "  Student ID: ");

        System.out.println("  Component: 1=Attestation 1  2=Attestation 2  3=Final Exam");
        String comp  = ConsoleHelper.readNonEmpty(scanner, "  Choice (1/2/3): ");
        double score = ConsoleHelper.readDouble(scanner, "  Score: ");

        try {
            switch (comp) {
                case "1" -> markService.setAtt1(studentId, courseCode, score);
                case "2" -> markService.setAtt2(studentId, courseCode, score);
                case "3" -> markService.setFinalExam(studentId, courseCode, score);
                default  -> { System.out.println("  [ERROR] Unknown component."); return; }
            }
            DataRepository.getInstance().save();
            System.out.println(Colors.green("  Mark saved."));
            markService.getMark(studentId, courseCode).ifPresent(m -> {
                System.out.println("  Current status: " + m);
                String component = comp.equals("1") ? "Att1" : comp.equals("2") ? "Att2" : "Final";
                LogService.getInstance().log(teacher.getId(), teacher.getFullName(),
                        "Entered " + component + " for " + studentId + " in " + courseCode
                        + ": " + score + " -> grade: " + m.getLetterGrade());
            });
        } catch (IllegalArgumentException e) {
            System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void viewCourseMarks() {
        List<Course> courses = courseService.getCoursesByInstructor(teacher.getId());
        if (courses.isEmpty()) { System.out.println("  No courses assigned to you."); return; }

        System.out.println(Colors.purple("\n  -- View Course Marks --"));
        for (int i = 0; i < courses.size(); i++)
            System.out.printf("  %d. [%-8s] %s%n", i + 1, courses.get(i).getCourseCode(), courses.get(i).getName());

        int idx = ConsoleHelper.readInt(scanner, "  Select course: ") - 1;
        if (idx < 0 || idx >= courses.size()) { System.out.println("  Invalid selection."); return; }

        String courseCode = courses.get(idx).getCourseCode();
        System.out.println("\n" + markService.generateMarkReport(courseCode));
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void viewCourseStudents() {
        List<Course> courses = courseService.getCoursesByInstructor(teacher.getId());
        if (courses.isEmpty()) { System.out.println("  No courses assigned to you."); return; }

        System.out.println("\n  Your courses:");
        for (int i = 0; i < courses.size(); i++)
            System.out.printf("  %d. [%-8s] %s%n", i + 1, courses.get(i).getCourseCode(), courses.get(i).getName());

        int idx = ConsoleHelper.readInt(scanner, "  Select course: ") - 1;
        if (idx < 0 || idx >= courses.size()) { System.out.println("  Invalid selection."); return; }
        String courseCode = courses.get(idx).getCourseCode();

        List<String> rows = new java.util.ArrayList<>();
        for (User u : DataRepository.getInstance().getUsers().values()) {
            if (u instanceof UniSpace.model.user.Student s &&
                    courseService.getStudentCourses(s.getId()).stream()
                            .anyMatch(c -> c.getCourseCode().equals(courseCode))) {
                rows.add(String.format("%-10s %s", s.getId(), s.getFullName()));
            }
        }
        ConsoleHelper.printTable("Students in " + courseCode, rows);
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void sendComplaint() {
        System.out.println(Colors.purple("\n  -- Send Complaint --"));
        User recipient = ConsoleHelper.selectUserFromDirectory(
                DataRepository.getInstance().getUsers().values(), teacher.getId(),
                java.util.EnumSet.of(UserRole.TEACHER, UserRole.STUDENT, UserRole.MANAGER),
                scanner);
        if (recipient == null) return;
        System.out.print("  Complaint text: ");
        String text = ConsoleHelper.readChoice(scanner);
        if (text.isEmpty()) { System.out.println("  [ERROR] Complaint text cannot be empty."); return; }
        try {
            ComplaintService.getInstance().submit(
                    teacher.getId(), teacher.getFullName(),
                    recipient.getId(), recipient.getFullName(), text);
            DataRepository.getInstance().save();
            System.out.println(Colors.green("  Complaint submitted successfully."));
        } catch (IllegalArgumentException e) {
            System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    // ── Researcher logic ──────────────────────────────────────────────────────

    private void handleResearcherOption() {
        if (teacher.isResearcher()) {
            openResearcherMode();
            return;
        }
        ResearcherRequestService rrs = ResearcherRequestService.getInstance();
        boolean hasPending = rrs.hasPendingRequest(teacher.getId());

        System.out.println("\n  You do not have the Researcher role.");
        if (hasPending) {
            System.out.println(Colors.yellow("  Your request is PENDING — waiting for manager review."));
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }

        List<ResearcherRequest> previous = rrs.getRequestsByApplicant(teacher.getId());
        if (!previous.isEmpty()) {
            ResearcherRequest last = previous.get(previous.size() - 1);
            if (last.getStatus() == ResearcherRequest.Status.REJECTED) {
                System.out.println(Colors.red("  Your last request was REJECTED."));
                if (last.getManagerNote() != null)
                    System.out.println(Colors.gray("  Reason: " + last.getManagerNote()));
            }
        }

        System.out.println(Colors.green("  1. Submit researcher role request"));
        System.out.println(Colors.gray("  0. <- Back"));
        System.out.print("  Choice: ");
        if ("1".equals(ConsoleHelper.readChoice(scanner))) {
            try {
                rrs.submitRequest(teacher.getId(), teacher.getFullName(), "TEACHER");
                DataRepository.getInstance().save();
                System.out.println(Colors.green("  Request submitted — a manager will review it shortly."));
            } catch (ValidationException e) {
                System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
            }
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void openResearcherMode() {
        new ResearcherMenu(
                teacher.getResearcherProfile(),
                teacher.getFaculty(),
                researchService
        ).show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void printMenu() {
        boolean isRes       = teacher.isResearcher();
        boolean isProfessor = teacher.getTitle() == TeacherTitle.PROFESSOR;
        boolean hasPending  = !isRes && !isProfessor
                && ResearcherRequestService.getInstance().hasPendingRequest(teacher.getId());
        String resLabel = (isProfessor || isRes) ? "Researcher mode"
                : hasPending ? "Researcher mode [request pending]"
                : "Researcher mode " + Colors.yellow("[submit request]");
        int unread = messageService.getUnreadCount(teacher.getId());

        AcademicYearService ays = AcademicYearService.getInstance();
        String yearStatus = ays.isYearClosed()
                ? Colors.red("CLOSED") : Colors.green("OPEN");
        System.out.println(Colors.gray("\n  ========================================"));
        System.out.println(Colors.bold("   Teacher Menu - " + teacher.getFullName()
                + " [" + teacher.getTitle() + "]"));
        System.out.println(Colors.gray("  Academic Year: " + ays.getCurrentYear()
                + "  [" + yearStatus + "]"));
        System.out.println(Colors.gray("  ========================================"));
        System.out.println("   1. View my courses");
        System.out.println("   2. Enter / update a mark");
        System.out.println("   3. View marks for a course");
        System.out.println("   4. View students in a course");
        System.out.println("   5. View inbox" + (unread > 0 ? Colors.yellow(" [" + unread + " unread]") : ""));
        System.out.println("   6. Send message");
        System.out.println("   7. Send complaint");
        System.out.printf ("   8. %s%n", resLabel);
        System.out.println("   0. Exit");
        System.out.print("  Choice: ");
    }
}
