package UniSpace.ui;

import UniSpace.exception.CourseRegistrationException;
import UniSpace.exception.ValidationException;
import UniSpace.model.course.Course;
import UniSpace.model.course.Mark;
import UniSpace.model.news.News;
import UniSpace.model.research.ResearcherRequest;
import UniSpace.model.user.Student;
import UniSpace.model.user.Teacher;
import UniSpace.model.user.User;
import UniSpace.model.course.RegistrationRequest;
import UniSpace.service.AuthService;
import UniSpace.service.CourseService;
import UniSpace.service.MarkService;
import UniSpace.service.MessageService;
import UniSpace.service.NewsService;
import UniSpace.service.RegistrationService;
import UniSpace.service.ResearchService;
import UniSpace.service.ResearcherRequestService;
import UniSpace.storage.DataRepository;
import UniSpace.util.Colors;
import UniSpace.util.ConsoleHelper;
import UniSpace.util.Paginator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Console menu for students.
 */
public class StudentMenu extends BaseMenu {

    private final Student         student;
    private final CourseService   courseService;
    private final MarkService     markService;
    private final ResearchService researchService;

    public StudentMenu(Student student, CourseService courseService,
                       MarkService markService, ResearchService researchService,
                       MessageService messageService) {
        super(messageService);
        this.student         = student;
        this.courseService   = courseService;
        this.markService     = markService;
        this.researchService = researchService;
    }

    @Override
    protected User currentUser() { return student; }

    public void show() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = ConsoleHelper.readChoice(scanner);
            switch (choice) {
                case "1"  -> registerCourse();
                case "2"  -> dropCourse();
                case "3"  -> viewMyCourses();
                case "4"  -> viewMyMarks();
                case "5"  -> printTranscript();
                case "6"  -> rateTeacher();
                case "7"  -> viewTeacherInfo();
                case "8"  -> viewInbox();
                case "9"  -> sendMessage();
                case "10" -> openResearcherModeOrNotify();
                case "11" -> viewNews();
                case "0"  -> running = false;
                default   -> System.out.println(Colors.red("  Invalid option. Try again."));
            }
        }
    }

    // ── Menu actions ──────────────────────────────────────────────────────────

    private void registerCourse() {
        Set<String> enrolledCodes = courseService.getStudentCourses(student.getId())
                .stream().map(Course::getCourseCode).collect(Collectors.toSet());

        List<Course> available = courseService.getAllCourses().stream()
                .filter(c -> !enrolledCodes.contains(c.getCourseCode()))
                .filter(c -> c.getTargetYear() == 0 || c.getTargetYear() == student.getYear())
                .filter(c -> c.getTargetFaculty() == null || c.getTargetFaculty() == student.getFaculty())
                .collect(Collectors.toList());

        int currentCredits = courseService.getStudentCredits(student.getId());
        System.out.println(Colors.purple("\n  -- Available Courses --")
                + Colors.gray(" (Credits used: " + currentCredits + " / " + Student.MAX_CREDITS + ")"));
        if (available.isEmpty()) {
            System.out.println("  No available courses for your year/faculty.");
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }
        System.out.printf("  %-8s %-32s %-8s %s%n", "Code", "Name", "Credits", "");
        System.out.println("  " + "-".repeat(60));
        for (Course c : available) {
            String warn = (currentCredits + c.getCredits() > Student.MAX_CREDITS)
                    ? Colors.red(" [EXCEEDS LIMIT]") : "";
            System.out.printf("  %-8s %-32s %-8d%s%n",
                    c.getCourseCode(), c.getName(), c.getCredits(), warn);
        }

        System.out.print("\n  Enter course code (0 = back): ");
        String code = scanner.nextLine().trim();
        if ("0".equals(code)) return;

        if (courseService.findCourse(code).isEmpty()) {
            System.out.println(Colors.red("  [ERROR] Course not found: " + code));
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }

        try {
            RegistrationRequest req = RegistrationService.getInstance()
                    .submitRequest(student.getId(), code);
            DataRepository.getInstance().save();
            System.out.println(Colors.green("  Request submitted — awaiting manager approval."));
            System.out.println(Colors.gray("  Request ID: " + req.getRequestId().substring(0, 8) + "..."));
        } catch (CourseRegistrationException e) {
            System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void dropCourse() {
        List<Course> myCourses = courseService.getStudentCourses(student.getId());
        Course course = Paginator.selectFromList(myCourses, "Drop a Course",
                c -> String.format("[%-8s] %s  (%d credits)", c.getCourseCode(), c.getName(), c.getCredits()),
                scanner);
        if (course == null) return;

        String code = course.getCourseCode();
        System.out.print(Colors.red("  Drop " + code + " — " + course.getName() + "? (yes/No): "));
        String confirm = ConsoleHelper.readChoice(scanner);
        if (!"yes".equalsIgnoreCase(confirm)) {
            System.out.println(Colors.gray("  Cancelled."));
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }
        try {
            courseService.dropCourse(student.getId(), code);
            DataRepository.getInstance().save();
            System.out.println(Colors.red("  Dropped: " + code + " — " + course.getName()));
        } catch (CourseRegistrationException e) {
            System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void viewMyCourses() {
        List<Course> courses = courseService.getStudentCourses(student.getId());
        if (courses.isEmpty()) { System.out.println("  No courses registered."); }
        else {
            System.out.println(Colors.purple("\n  -- Your Courses --"));
            int total = 0;
            for (Course c : courses) {
                System.out.printf("    %-10s %-30s %d credits%n",
                        c.getCourseCode(), c.getName(), c.getCredits());
                total += c.getCredits();
            }
            System.out.println("  Total credits: " + total + " / " + Student.MAX_CREDITS);
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void viewMyMarks() {
        List<Course> courses = courseService.getStudentCourses(student.getId());
        if (courses.isEmpty()) {
            System.out.println("  No courses registered.");
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }

        Course course = Paginator.selectFromList(courses, "View Marks - Select Course",
                c -> String.format("[%-8s] %s  (%d credits)", c.getCourseCode(), c.getName(), c.getCredits()),
                scanner);
        if (course == null) return;

        System.out.println(Colors.purple("\n  -- Marks: " + course.getCourseCode() + " -- " + course.getName() + " --"));

        java.util.Optional<Mark> opt = markService.getMark(student.getId(), course.getCourseCode());
        if (opt.isEmpty()) {
            System.out.println(Colors.yellow("  No marks recorded yet for this course."));
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }

        Mark m = opt.get();
        String na = Colors.gray("--");

        String att1Str  = m.getAtt1()      != null ? Colors.accent(String.format("%.1f", m.getAtt1()))      : na;
        String att2Str  = m.getAtt2()      != null ? Colors.accent(String.format("%.1f", m.getAtt2()))      : na;
        String finalStr = m.getFinalExam() != null ? Colors.accent(String.format("%.1f", m.getFinalExam())) : na;

        System.out.println("  " + String.format("%-22s", "Attestation 1:")  + att1Str  + Colors.gray(" / 30"));
        System.out.println("  " + String.format("%-22s", "Attestation 2:")  + att2Str  + Colors.gray(" / 30"));

        if (m.getAtt1() != null && m.getAtt2() != null) {
            double attTotal = m.getAtt1() + m.getAtt2();
            String admitLabel = m.isAdmittedToFinal()
                    ? "  " + Colors.green("[Admitted to Final]")
                    : "  " + Colors.red("[NOT Admitted]");
            System.out.println("  " + String.format("%-22s", "Attestation Total:")
                    + Colors.accent(String.format("%.1f", attTotal)) + Colors.gray(" / 60") + admitLabel);
        }

        System.out.println("  " + String.format("%-22s", "Final Exam:")     + finalStr + Colors.gray(" / 40"));
        System.out.println(Colors.gray("  " + "-".repeat(40)));

        Double total = m.getTotal();
        String totalStr = total != null
                ? Colors.accent(String.format("%.1f", total)) + Colors.gray(" / 100")
                : Colors.gray("N/A");
        System.out.println("  " + String.format("%-22s", "Total Score:")   + totalStr);

        String grade = m.getLetterGrade();
        String gradeColored = switch (grade) {
            case "N/A" -> Colors.gray("N/A  (marks pending)");
            case "F"   -> Colors.red("F    (fail - retake course)");
            case "FX"  -> Colors.yellow("FX   (retake final exam)");
            default    -> Colors.accent(grade);
        };
        System.out.println("  " + String.format("%-22s", "Letter Grade:")  + gradeColored);

        if (m.isPassed()) {
            System.out.println("  " + String.format("%-22s", "GPA Points:")
                    + Colors.accent(String.format("%.2f", m.getGradePoint())) + Colors.gray(" / 4.00"));
            System.out.println("  " + String.format("%-22s", "Status:") + Colors.green("Passed"));
        } else if ("FX".equals(grade)) {
            System.out.println("  " + String.format("%-22s", "Status:") + Colors.yellow("FX - retake final exam"));
        } else if ("F".equals(grade)) {
            System.out.println("  " + String.format("%-22s", "Status:") + Colors.red("Failed - retake course"));
        } else {
            System.out.println("  " + String.format("%-22s", "Status:") + Colors.gray("In progress"));
        }

        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void printTranscript() {
        System.out.println(markService.generateTranscript(student.getId()));
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void rateTeacher() {
        List<Course> myCourses = courseService.getStudentCourses(student.getId());
        if (myCourses.isEmpty()) {
            System.out.println("  You have no enrolled courses.");
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }

        Course course = Paginator.selectFromList(myCourses, "Rate a Teacher - Select Course",
                c -> String.format("[%-8s] %s", c.getCourseCode(), c.getName()),
                scanner);
        if (course == null) return;

        List<Teacher> teachers = new java.util.ArrayList<>();
        for (String tid : course.getInstructorIds()) {
            User u = AuthService.getInstance().getUserById(tid);
            if (u instanceof Teacher t) teachers.add(t);
        }

        if (teachers.isEmpty()) {
            System.out.println(Colors.yellow("  No teachers assigned to this course."));
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }

        Teacher teacher = Paginator.selectFromList(teachers, "Rate a Teacher - Select Teacher",
                t -> {
                    String rated = t.hasRatedBy(student.getId())
                            ? Colors.yellow("  [rated - select to update]") : "";
                    return String.format("%-12s %-24s Rating: %.1f%s",
                            t.getId(), t.getFullName(), t.getRating(), rated);
                },
                scanner);
        if (teacher == null) return;

        if (teacher.hasRatedBy(student.getId()))
            System.out.println(Colors.yellow("  Note: you have already rated this teacher - rating will be updated."));

        double rating;
        while (true) {
            rating = ConsoleHelper.readDouble(scanner, "  Rating 1-5 (0 = back): ");
            if (rating == 0) return;
            if (rating >= 1 && rating <= 5) break;
            System.out.println(Colors.red("  [ERROR] Rating must be between 1 and 5."));
        }

        try {
            teacher.addRating(student.getId(), rating);
            DataRepository.getInstance().save();
            System.out.println(Colors.green(String.format(
                    "  Rating %.1f submitted for %s.", rating, teacher.getFullName())));
        } catch (ValidationException e) {
            System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void viewTeacherInfo() {
        List<Course> myCourses = courseService.getStudentCourses(student.getId());
        if (myCourses.isEmpty()) { System.out.println("  You have no enrolled courses."); return; }

        System.out.println("\n  Your courses:");
        for (int i = 0; i < myCourses.size(); i++)
            System.out.printf("  %d. [%-8s] %s%n", i + 1, myCourses.get(i).getCourseCode(), myCourses.get(i).getName());

        int idx = ConsoleHelper.readInt(scanner, "  Select course: ") - 1;
        if (idx < 0 || idx >= myCourses.size()) { System.out.println("  Invalid selection."); return; }
        Course course = myCourses.get(idx);

        List<String> rows = new java.util.ArrayList<>();
        for (String tid : course.getInstructorIds()) {
            User u = AuthService.getInstance().getUserById(tid);
            if (u instanceof Teacher t)
                rows.add(String.format("%-10s %-20s %s (Rating: %.1f)", t.getId(), t.getFullName(), t.getTitle(), t.getRating()));
        }
        ConsoleHelper.printTable("Instructors for " + course.getCourseCode(), rows);
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void openResearcherModeOrNotify() {
        if (student.isResearcher()) {
            new ResearcherMenu(student.getResearcherProfile(), student.getFaculty(), researchService).show();
            return;
        }
        ResearcherRequestService rrs = ResearcherRequestService.getInstance();
        boolean hasPending = rrs.hasPendingRequest(student.getId());

        System.out.println("\n  You do not have the Researcher role.");
        if (hasPending) {
            System.out.println(Colors.yellow("  Your request is PENDING — waiting for manager review."));
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }

        List<ResearcherRequest> previous = rrs.getRequestsByApplicant(student.getId());
        if (!previous.isEmpty()) {
            ResearcherRequest last = previous.get(previous.size() - 1);
            if (last.getStatus() == ResearcherRequest.Status.REJECTED) {
                System.out.println(Colors.red("  Your last request was REJECTED."));
                if (last.getManagerNote() != null)
                    System.out.println(Colors.gray("  Reason: " + last.getManagerNote()));
            }
        }

        System.out.println(Colors.green("  1.") + " Submit researcher role request");
        System.out.println(Colors.gray("  0. <- Back"));
        System.out.print("  Choice: ");
        if ("1".equals(ConsoleHelper.readChoice(scanner))) {
            try {
                rrs.submitRequest(student.getId(), student.getFullName(), "STUDENT");
                DataRepository.getInstance().save();
                System.out.println(Colors.green("  Request submitted — a manager will review it shortly."));
            } catch (ValidationException e) {
                System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
            }
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void viewNews() {
        List<News> news = NewsService.getInstance().getAllNews();
        if (news.isEmpty()) {
            System.out.println("\n  No news published yet.");
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }
        Paginator.viewList(news, "University News",
                n -> Colors.accent(n.getTitle())
                        + Colors.gray("  — " + n.getAuthorName() + "  " + n.getDate())
                        + (n.getContent() != null && !n.getContent().isEmpty()
                        ? "\n    " + n.getContent() : ""),
                scanner);
    }

    private void printMenu() {
        boolean isRes      = student.isResearcher();
        int     unread     = messageService.getUnreadCount(student.getId());
        boolean hasPending = !isRes && ResearcherRequestService.getInstance().hasPendingRequest(student.getId());
        String  resLabel   = isRes ? "Researcher mode"
                : hasPending ? Colors.yellow("Researcher mode [request pending]")
                : "Researcher mode " + Colors.yellow("[submit request]");

        System.out.println(Colors.gray("\n  ========================================"));
        System.out.println(Colors.bold("   Student Menu - " + student.getFullName()));
        System.out.println(Colors.gray("  ========================================"));
        System.out.println("   1. Register for a course");
        System.out.println("   2. Drop a course");
        System.out.println("   3. View my courses");
        System.out.println("   4. View my marks");
        System.out.println("   5. Print transcript");
        System.out.println("   6. Rate a teacher");
        System.out.println("   7. View teacher info");
        System.out.println("   8. View inbox" + (unread > 0 ? Colors.yellow(" [" + unread + " unread]") : ""));
        System.out.println("   9. Send message");
        System.out.println("   10. " + resLabel);
        System.out.println("   11. View news");
        System.out.println("   0. Exit");
        System.out.print("  Choice: ");
    }
}
