package UniSpace.ui;

import UniSpace.enums.Faculty;
import UniSpace.enums.UserRole;
import UniSpace.exception.CourseRegistrationException;
import UniSpace.exception.HIndexException;
import UniSpace.exception.ValidationException;
import UniSpace.model.course.Course;
import UniSpace.model.course.RegistrationRequest;
import UniSpace.model.news.News;
import UniSpace.model.research.ResearcherProfile;
import UniSpace.model.research.ResearcherRequest;
import UniSpace.model.user.Employee;
import UniSpace.model.user.Manager;
import UniSpace.model.user.Student;
import UniSpace.model.user.Teacher;
import UniSpace.model.user.User;
import UniSpace.service.*;
import UniSpace.storage.DataRepository;
import UniSpace.util.Colors;
import UniSpace.util.ConsoleHelper;
import UniSpace.util.Paginator;

import java.util.*;
import java.util.stream.Collectors;

public class ManagerMenu extends BaseMenu {

    private final Manager             manager;
    private final CourseService       courseService;
    private final MarkService         markService;
    private final RegistrationService registrationService;
    private final NewsService         newsService;
    private final AuthService         authService;

    public ManagerMenu(Manager manager,
                       CourseService courseService,
                       MarkService markService,
                       RegistrationService registrationService,
                       NewsService newsService,
                       MessageService messageService) {
        super(messageService);
        this.manager             = manager;
        this.courseService       = courseService;
        this.markService         = markService;
        this.registrationService = registrationService;
        this.newsService         = newsService;
        this.authService         = AuthService.getInstance();
    }

    @Override
    protected User currentUser() { return manager; }

    // ── Main loop ─────────────────────────────────────────────────────────────

    public void show() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = ConsoleHelper.readChoice(scanner);
            switch (choice) {
                case "1"  -> manageRegistrationRequests();
                case "2"  -> manageCourses();
                case "3"  -> viewStudents();
                case "4"  -> viewTeachers();
                case "5"  -> statisticalReports();
                case "6"  -> manageNews();
                case "7"  -> viewInbox();
                case "8"  -> sendMessage();
                case "9"  -> assignSupervisor();
                case "10" -> manageResearcherRequests();
                case "0"  -> running = false;
                default   -> System.out.println(Colors.red("  Invalid option."));
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  1. REGISTRATION REQUESTS
    // ══════════════════════════════════════════════════════════════════════════

    private void manageRegistrationRequests() {
        boolean back = false;
        while (!back) {
            int pendingCount = registrationService.getPendingRequests().size();
            System.out.println(Colors.purple("\n  -- Registration Requests --")
                    + (pendingCount > 0 ? Colors.yellow(" (" + pendingCount + " pending)") : ""));
            System.out.println("   1. Process a pending request");
            System.out.println("   2. View all requests (history)");
            System.out.println(Colors.gray("   0. <- Back"));
            System.out.print("  Choice: ");

            switch (ConsoleHelper.readChoice(scanner)) {
                case "1" -> processRegistrationRequest();
                case "2" -> viewAllRequests();
                case "0" -> back = true;
                default  -> System.out.println(Colors.red("  Invalid option."));
            }
        }
    }

    private void processRegistrationRequest() {
        List<RegistrationRequest> pending = registrationService.getPendingRequests();
        if (pending.isEmpty()) {
            System.out.println("  No pending registration requests.");
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }
        RegistrationRequest req = Paginator.selectFromList(pending, "Pending Registration Requests",
                r -> String.format("[%s] [%s] Student %-8s -> %-8s  (%s)",
                        r.getRequestId().substring(0, 8), r.getStatus(),
                        r.getStudentId(), r.getCourseCode(),
                        r.getRequestDate().toLocalDate()),
                scanner);
        if (req == null) return;

        System.out.println();
        System.out.println(Colors.green("   1. Approve") + "   " + Colors.red("  2. Reject")
                + "   " + Colors.gray("  0. Cancel"));
        System.out.print("  Choice: ");

        switch (ConsoleHelper.readChoice(scanner)) {
            case "1" -> {
                try {
                    RegistrationRequest approved = registrationService.approveRequest(req.getRequestId(), courseService);
                    DataRepository.getInstance().save();
                    System.out.println(Colors.green("  Approved: " + approved.getStudentId() + " -> " + approved.getCourseCode()));
                } catch (CourseRegistrationException e) {
                    System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
                }
            }
            case "2" -> {
                System.out.print("  Reason (optional): ");
                String reason = ConsoleHelper.readChoice(scanner);
                try {
                    registrationService.rejectRequest(req.getRequestId(), reason.isEmpty() ? null : reason);
                    DataRepository.getInstance().save();
                    System.out.println(Colors.yellow("  Request rejected."));
                } catch (CourseRegistrationException e) {
                    System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
                }
            }
            default -> System.out.println(Colors.gray("  Cancelled."));
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void viewAllRequests() {
        List<RegistrationRequest> all = new ArrayList<>(registrationService.getAllRequests());
        Paginator.viewList(all, "All Registration Requests", RegistrationRequest::toString, scanner);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  2. MANAGE COURSES
    // ══════════════════════════════════════════════════════════════════════════

    private void manageCourses() {
        boolean back = false;
        while (!back) {
            System.out.println(Colors.purple("\n  -- Manage Courses --"));
            System.out.println("   1. Add new course");
            System.out.println("   2. View all courses");
            System.out.println("   3. Assign teacher to course");
            System.out.println("   4. Course details");
            System.out.println("   5. Edit course");
            System.out.println(Colors.gray("   0. <- Back"));
            System.out.print("  Choice: ");

            switch (ConsoleHelper.readChoice(scanner)) {
                case "1" -> addCourse();
                case "2" -> viewAllCourses();
                case "3" -> assignTeacher();
                case "4" -> viewCourseDetails();
                case "5" -> editCourse();
                case "0" -> back = true;
                default  -> System.out.println(Colors.red("  Invalid option."));
            }
        }
    }

    private void editCourse() {
        Course course = Paginator.selectFromList(new ArrayList<>(courseService.getAllCourses()),
                "Select Course to Edit",
                c -> String.format("%-8s %-30s %d credits  Faculty: %s  Y%s",
                        c.getCourseCode(), c.getName(), c.getCredits(),
                        c.getTargetFaculty() != null ? c.getTargetFaculty() : "All",
                        c.getTargetYear() > 0 ? String.valueOf(c.getTargetYear()) : "All"),
                scanner);
        if (course == null) return;

        System.out.println("  Current: " + course.getName()
                + " | " + course.getCredits() + " credits"
                + " | Year: " + (course.getTargetYear() > 0 ? course.getTargetYear() : "All")
                + " | Faculty: " + (course.getTargetFaculty() != null ? course.getTargetFaculty() : "All"));

        System.out.print("  New name (Enter to keep): ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) course.setName(name);

        System.out.print("  New credits (Enter to keep): ");
        String credStr = scanner.nextLine().trim();
        if (!credStr.isEmpty()) {
            try { course.setCredits(Integer.parseInt(credStr)); }
            catch (NumberFormatException e) { System.out.println("  [WARN] Invalid credits, skipped."); }
        }

        System.out.print("  New target year 1-4 (0=all, Enter to keep): ");
        String yearStr = scanner.nextLine().trim();
        if (!yearStr.isEmpty()) {
            try {
                int yr = Integer.parseInt(yearStr);
                if (yr >= 0 && yr <= 4) course.setTargetYear(yr);
            } catch (NumberFormatException ignored) {}
        }

        System.out.print("  Change faculty? (y/n): ");
        if ("y".equalsIgnoreCase(scanner.nextLine().trim())) {
            Faculty fac = pickFaculty("  New faculty (0=all): ", true);
            course.setTargetFaculty(fac);
        }

        DataRepository.getInstance().save();
        System.out.println(Colors.green("  Course updated: " + course.getCourseCode()));
    }

    private void addCourse() {
        System.out.print("  Course code: ");
        String code = scanner.nextLine().trim();
        System.out.print("  Course name: ");
        String name = scanner.nextLine().trim();
        System.out.print("  Credits: ");
        int credits;
        try { credits = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("  [ERROR] Invalid credits."); return; }

        Course course = new Course(code, name, credits);

        Faculty faculty = pickFaculty("  Target faculty (0 = open to all): ", true);
        course.setTargetFaculty(faculty);

        System.out.print("  Target year 1-4 (0 = all): ");
        try {
            int year = Integer.parseInt(scanner.nextLine().trim());
            if (year >= 0 && year <= 4) course.setTargetYear(year);
        } catch (NumberFormatException ignored) {}

        courseService.addCourse(course);
        DataRepository.getInstance().save();
        System.out.println(Colors.green("  Course added: " + course.getCourseCode()));
    }

    private void viewAllCourses() {
        Collection<Course> courses = courseService.getAllCourses();
        if (courses.isEmpty()) { System.out.println("  No courses found."); return; }
        System.out.println("\n  -- All Courses --");
        System.out.printf("  %-8s %-32s %-8s %-24s %s%n",
                "Code", "Name", "Credits", "Faculty", "Year");
        System.out.println("  " + "-".repeat(85));
        for (Course c : courses) {
            String fac  = c.getTargetFaculty() != null ? c.getTargetFaculty().toString() : "All";
            String year = c.getTargetYear() > 0 ? "Y" + c.getTargetYear() : "All";
            System.out.printf("  %-8s %-32s %-8d %-24s %s%n",
                    c.getCourseCode(), c.getName(), c.getCredits(), fac, year);
        }
    }

    private void assignTeacher() {
        Course course = Paginator.selectFromList(new ArrayList<>(courseService.getAllCourses()),
                "Select Course",
                c -> String.format("%-8s %-30s %d credits", c.getCourseCode(), c.getName(), c.getCredits()),
                scanner);
        if (course == null) return;

        Teacher t = Paginator.selectFromList(getAllTeachers(), "Select Teacher",
                tc -> String.format("%-8s %-22s %s", tc.getId(), tc.getFullName(), tc.getTitle()),
                scanner);
        if (t == null) return;

        try {
            courseService.assignInstructor(course.getCourseCode(), t.getId());
            t.addCourse(course);
            DataRepository.getInstance().save();
            System.out.println(Colors.green("  " + t.getFullName() + " assigned to " + course.getCourseCode()));
        } catch (CourseRegistrationException e) {
            System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
        }
    }

    private void viewCourseDetails() {
        Course c = Paginator.selectFromList(new ArrayList<>(courseService.getAllCourses()),
                "Select Course",
                course -> String.format("%-8s %-30s %d credits", course.getCourseCode(), course.getName(), course.getCredits()),
                scanner);
        if (c == null) return;
        System.out.println("\n  -- Course Details --");
        System.out.println("  Code    : " + c.getCourseCode());
        System.out.println("  Name    : " + c.getName());
        System.out.println("  Credits : " + c.getCredits());
        System.out.println("  Faculty : " + (c.getTargetFaculty() != null ? c.getTargetFaculty() : "Open to all"));
        System.out.println("  Year    : " + (c.getTargetYear() > 0 ? "Y" + c.getTargetYear() : "Open to all"));
        System.out.println("  Teachers: " + c.getInstructorIds());
        System.out.println("  Lessons : " + c.getLessons().size());
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  3. VIEW STUDENTS
    // ══════════════════════════════════════════════════════════════════════════

    private void viewStudents() {
        boolean back = false;
        while (!back) {
            System.out.println(Colors.purple("\n  -- View Students --"));
            System.out.println("   1. All students by GPA (descending)");
            System.out.println("   2. All students alphabetically");
            System.out.println("   3. Students by faculty");
            System.out.println("   4. Student details");
            System.out.println(Colors.gray("   0. <- Back"));
            System.out.print("  Choice: ");

            switch (ConsoleHelper.readChoice(scanner)) {
                case "1" -> listStudents(Comparator.comparingDouble(
                        s -> -markService.getGpa(s.getId())));
                case "2" -> listStudents(Comparator.<Student, String>comparing(User::getLastName)
                        .thenComparing(User::getFirstName));
                case "3" -> listStudentsByFaculty();
                case "4" -> viewStudentDetails();
                case "0" -> back = true;
                default  -> System.out.println(Colors.red("  Invalid option."));
            }
        }
    }

    private void listStudents(Comparator<Student> comparator) {
        List<Student> students = getAllStudents();
        if (students.isEmpty()) { System.out.println("  No students found."); return; }
        students.sort(comparator);
        Paginator.viewList(students, "Students",
                s -> String.format("%-8s %-22s  Y%-2d  %-20s  GPA %.2f",
                        s.getId(), s.getFullName(), s.getYear(),
                        s.getFaculty(), markService.getGpa(s.getId())),
                scanner);
    }

    private void listStudentsByFaculty() {
        Faculty f = pickFaculty("  Faculty: ", false);
        if (f == null) return;
        List<Student> students = getAllStudents().stream()
                .filter(s -> f == s.getFaculty())
                .sorted(Comparator.comparing(User::getLastName))
                .collect(Collectors.toList());
        if (students.isEmpty()) { System.out.println("  No students in " + f); return; }
        Paginator.viewList(students, "Students — " + f,
                s -> String.format("%-8s %-22s  Y%-2d  GPA %.2f",
                        s.getId(), s.getFullName(), s.getYear(),
                        markService.getGpa(s.getId())),
                scanner);
    }

    private void viewStudentDetails() {
        Student s = Paginator.selectFromList(getAllStudents(), "Select Student",
                st -> String.format("%-8s %-22s  Y%-2d  %-20s  GPA %.2f",
                        st.getId(), st.getFullName(), st.getYear(),
                        st.getFaculty(), markService.getGpa(st.getId())),
                scanner);
        if (s == null) return;
        System.out.println("\n  -- Student Details --");
        System.out.println("  " + s);
        System.out.println("  Credits enrolled : " + courseService.getStudentCredits(s.getId()));
        System.out.println("  Cumulative GPA   : " + String.format("%.2f", markService.getGpa(s.getId())));
        System.out.println("\n" + markService.generateTranscript(s.getId()));
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  4. VIEW TEACHERS
    // ══════════════════════════════════════════════════════════════════════════

    private void viewTeachers() {
        boolean back = false;
        while (!back) {
            System.out.println(Colors.purple("\n  -- View Teachers --"));
            System.out.println("   1. By rating (descending)");
            System.out.println("   2. Alphabetically");
            System.out.println("   3. Teacher details");
            System.out.println(Colors.gray("   0. <- Back"));
            System.out.print("  Choice: ");

            switch (ConsoleHelper.readChoice(scanner)) {
                case "1" -> listTeachers(Comparator.comparingDouble(
                        (Teacher t) -> t.getRating()).reversed());
                case "2" -> listTeachers(Comparator.<Teacher, String>comparing(User::getLastName)
                        .thenComparing(User::getFirstName));
                case "3" -> viewTeacherDetails();
                case "0" -> back = true;
                default  -> System.out.println(Colors.red("  Invalid option."));
            }
        }
    }

    private void listTeachers(Comparator<Teacher> comparator) {
        List<Teacher> teachers = getAllTeachers();
        if (teachers.isEmpty()) { System.out.println("  No teachers found."); return; }
        teachers.sort(comparator);
        Paginator.viewList(teachers, "Teachers",
                t -> String.format("%-8s %-22s %-16s  Rating: %.1f  Researcher: %s",
                        t.getId(), t.getFullName(), t.getTitle(),
                        t.getRating(), t.isResearcher() ? "Yes" : "No"),
                scanner);
    }

    private void viewTeacherDetails() {
        Teacher t = Paginator.selectFromList(getAllTeachers(), "Select Teacher",
                tc -> String.format("%-8s %-22s %-16s  Rating: %.1f",
                        tc.getId(), tc.getFullName(), tc.getTitle(), tc.getRating()),
                scanner);
        if (t == null) return;
        System.out.println("\n  -- Teacher Details --");
        System.out.println("  " + t);
        List<Course> courses = courseService.getCoursesByInstructor(t.getId());
        System.out.println("  Courses (" + courses.size() + "): "
                + courses.stream().map(Course::getCourseCode).collect(Collectors.joining(", ")));
        if (t.isResearcher()) {
            System.out.println("  Researcher: YES | Papers: "
                    + t.getResearcherProfile().getResearchPapers().size()
                    + " | h-index: " + t.getResearcherProfile().getHIndex());
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  5. STATISTICAL REPORTS
    // ══════════════════════════════════════════════════════════════════════════

    private void statisticalReports() {
        boolean back = false;
        while (!back) {
            System.out.println(Colors.purple("\n  -- Statistical Reports --"));
            System.out.println("   1. Mark report for a course");
            System.out.println("   2. GPA distribution (all students)");
            System.out.println("   3. Top-N students by GPA");
            System.out.println("   4. Students with at least one failed course");
            System.out.println(Colors.gray("   0. <- Back"));
            System.out.print("  Choice: ");

            switch (ConsoleHelper.readChoice(scanner)) {
                case "1" -> courseMarkReport();
                case "2" -> gpaDistribution();
                case "3" -> topStudentsByGpa();
                case "4" -> studentsWithFails();
                case "0" -> back = true;
                default  -> System.out.println(Colors.red("  Invalid option."));
            }
        }
    }

    private void courseMarkReport() {
        Course c = Paginator.selectFromList(new ArrayList<>(courseService.getAllCourses()),
                "Select Course for Report",
                course -> String.format("%-8s %-30s %d credits",
                        course.getCourseCode(), course.getName(), course.getCredits()),
                scanner);
        if (c == null) return;
        System.out.println(markService.generateMarkReport(c.getCourseCode()));
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void gpaDistribution() {
        List<Student> students = getAllStudents();
        if (students.isEmpty()) { System.out.println("  No students found."); return; }

        int count4  = 0, count3 = 0, count2 = 0, count1 = 0, count0 = 0;
        for (Student s : students) {
            double gpa = markService.getGpa(s.getId());
            if      (gpa >= 3.67) count4++;
            else if (gpa >= 3.00) count3++;
            else if (gpa >= 2.00) count2++;
            else if (gpa >  0.00) count1++;
            else                  count0++;
        }
        System.out.println("\n  -- GPA Distribution --");
        System.out.printf("  A  (>= 3.67) : %d students%n", count4);
        System.out.printf("  B  (3.00-3.66): %d students%n", count3);
        System.out.printf("  C  (2.00-2.99): %d students%n", count2);
        System.out.printf("  D  (0.01-1.99): %d students%n", count1);
        System.out.printf("  No grades yet : %d students%n", count0);
        System.out.printf("  Total         : %d students%n", students.size());
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void topStudentsByGpa() {
        System.out.print("  Show top N students: ");
        int n;
        try { n = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("  [ERROR] Enter a number."); return; }

        List<Student> sorted = getAllStudents().stream()
                .sorted(Comparator.comparingDouble(s -> -markService.getGpa(s.getId())))
                .limit(n)
                .collect(Collectors.toList());

        System.out.println("\n  -- Top" + n + " Students --");
        int rank = 1;
        for (Student s : sorted) {
            System.out.printf("  %2d. %-22s GPA: %.2f  %s Y%d%n",
                    rank++, s.getFullName(), markService.getGpa(s.getId()),
                    s.getFaculty(), s.getYear());
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void studentsWithFails() {
        List<Student> withFails = getAllStudents().stream()
                .filter(s -> courseService.getStudentCourses().entrySet().stream()
                        .filter(e -> e.getKey().equals(s.getId()))
                        .flatMap(e -> e.getValue().stream())
                        .anyMatch(code -> courseService.getFailCount(s.getId(), code) > 0))
                .collect(Collectors.toList());

        System.out.println("\n  Students with failed courses: " + withFails.size());
        for (Student s : withFails) {
            System.out.printf("  %-8s %-22s%n", s.getId(), s.getFullName());
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  6. MANAGE NEWS
    // ══════════════════════════════════════════════════════════════════════════

    private void manageNews() {
        boolean back = false;
        while (!back) {
            System.out.println(Colors.purple("\n  -- Manage News --"));
            System.out.println("   1. Add news");
            System.out.println("   2. View all news");
            System.out.println("   3. Remove news");
            System.out.println(Colors.gray("   0. <- Back"));
            System.out.print("  Choice: ");

            switch (ConsoleHelper.readChoice(scanner)) {
                case "1" -> addNews();
                case "2" -> viewAllNews();
                case "3" -> removeNews();
                case "0" -> back = true;
                default  -> System.out.println(Colors.red("  Invalid option."));
            }
        }
    }

    private void addNews() {
        System.out.print("  Title: ");
        String title = ConsoleHelper.readChoice(scanner);
        System.out.print("  Content: ");
        String content = ConsoleHelper.readChoice(scanner);
        if (title.isEmpty()) { System.out.println("  [ERROR] Title cannot be empty."); return; }
        News news = newsService.addNews(title, content, manager.getId(), manager.getFullName());
        DataRepository.getInstance().save();
        System.out.println("  News published: " + news);
    }

    private void viewAllNews() {
        List<News> news = newsService.getAllNews();
        if (news.isEmpty()) { System.out.println("  No news published yet."); return; }
        Paginator.viewList(news, "News",
                n -> Colors.gray("[" + n.getNewsId().substring(0, 8) + "]  ")
                        + Colors.accent(n.getTitle())
                        + Colors.gray("  — " + n.getAuthorName() + "  " + n.getDate()),
                scanner);
    }

    private void removeNews() {
        viewAllNews();
        System.out.print("  News ID (first 8 chars): ");
        String prefix = ConsoleHelper.readChoice(scanner);
        News target = newsService.getAllNews().stream()
                .filter(n -> n.getNewsId().startsWith(prefix))
                .findFirst().orElse(null);
        if (target == null) { System.out.println("  News not found."); return; }
        newsService.removeNews(target.getNewsId());
        DataRepository.getInstance().save();
        System.out.println("  News removed.");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  7–8. MESSAGING
    // ══════════════════════════════════════════════════════════════════════════

    // ══════════════════════════════════════════════════════════════════════════
    //  10. ACTIVATE RESEARCHER ROLE
    // ══════════════════════════════════════════════════════════════════════════

    private void activateResearcherRole() {
        System.out.println("\n  -- Activate Researcher Role --");
        System.out.println("  Select user type: 1. Teacher  2. Student");
        System.out.print("  Choice: ");
        String typeChoice = scanner.nextLine().trim();

        List<User> candidates = new ArrayList<>();
        if ("1".equals(typeChoice)) {
            getAllTeachers().stream()
                    .filter(t -> !t.isResearcher())
                    .forEach(candidates::add);
        } else if ("2".equals(typeChoice)) {
            getAllStudents().stream()
                    .filter(s -> !s.isResearcher())
                    .forEach(candidates::add);
        } else {
            System.out.println("  Invalid choice.");
            return;
        }

        if (candidates.isEmpty()) {
            System.out.println("  No eligible users (all already researchers).");
            return;
        }

        System.out.printf("  %-10s %-25s %-12s%n", "ID", "Name", "Role");
        System.out.println("  " + "-".repeat(50));
        candidates.forEach(u -> System.out.printf("  %-10s %-25s %-12s%n",
                u.getId(), u.getFullName(), u.getRole()));

        System.out.print("  Enter user ID to activate: ");
        String userId = scanner.nextLine().trim();
        User target = authService.getUserById(userId);

        if (target instanceof UniSpace.model.user.Employee e) {
            if (e.isResearcher()) { System.out.println("  Already a researcher."); return; }
            e.activateResearcher();
            UniSpace.service.ResearchService.getInstance().addResearcher(e.getResearcherProfile());
            DataRepository.getInstance().save();
            System.out.println("  Researcher role activated for " + e.getFullName());
        } else if (target instanceof UniSpace.model.user.Student s) {
            if (s.isResearcher()) { System.out.println("  Already a researcher."); return; }
            s.activateResearcher();
            UniSpace.service.ResearchService.getInstance().addResearcher(s.getResearcherProfile());
            DataRepository.getInstance().save();
            System.out.println("  Researcher role activated for " + s.getFullName());
        } else {
            System.out.println("  User not found or invalid.");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  11. RESEARCHER ROLE REQUESTS
    // ══════════════════════════════════════════════════════════════════════════

    private void manageResearcherRequests() {
        ResearcherRequestService rrs = ResearcherRequestService.getInstance();
        boolean back = false;
        while (!back) {
            int pending = rrs.getPendingRequests().size();
            System.out.println(Colors.purple("\n  -- Researcher Role Requests --")
                    + (pending > 0 ? Colors.yellow(" (" + pending + " pending)") : ""));
            System.out.println("   1. Process a pending request");
            System.out.println("   2. View all requests");
            System.out.println(Colors.gray("   0. <- Back"));
            System.out.print("  Choice: ");

            switch (ConsoleHelper.readChoice(scanner)) {
                case "1" -> processResearcherRequest(rrs);
                case "2" -> viewAllResearcherRequests(rrs);
                case "0" -> back = true;
                default  -> System.out.println(Colors.red("  Invalid option."));
            }
        }
    }

    private void processResearcherRequest(ResearcherRequestService rrs) {
        List<ResearcherRequest> pending = rrs.getPendingRequests();
        if (pending.isEmpty()) {
            System.out.println("  No pending researcher requests.");
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }

        ResearcherRequest req = Paginator.selectFromList(pending, "Pending Researcher Requests",
                r -> String.format("[%s]  %-22s  (%s)  — %s",
                        r.getRequestId().substring(0, 8),
                        r.getApplicantName(), r.getApplicantRole(),
                        r.getSubmittedAt().toLocalDate()),
                scanner);
        if (req == null) return;

        System.out.println(Colors.purple("\n  -- Request Details --"));
        System.out.println("  Applicant : " + req.getApplicantName() + " (" + req.getApplicantRole() + ")");
        System.out.println("  Submitted : " + Colors.gray(req.getSubmittedAt().toLocalDate().toString()));
        System.out.println();
        System.out.println(Colors.green("   1. Approve"));
        System.out.println(Colors.red("   2. Reject"));
        System.out.println(Colors.gray("   0. Cancel"));
        System.out.print("  Choice: ");

        switch (ConsoleHelper.readChoice(scanner)) {
            case "1" -> {
                try {
                    rrs.approveRequest(req.getRequestId());
                    User target = authService.getUserById(req.getApplicantId());
                    if (target instanceof Employee e) {
                        e.activateResearcher();
                        ResearchService.getInstance().addResearcher(e.getResearcherProfile());
                    } else if (target instanceof Student s) {
                        s.activateResearcher();
                        ResearchService.getInstance().addResearcher(s.getResearcherProfile());
                    }
                    messageService.send(manager.getId(), manager.getFullName(),
                            req.getApplicantId(),
                            "Your researcher role request has been APPROVED. You can now access Researcher mode.");
                    DataRepository.getInstance().save();
                    System.out.println(Colors.green("  Approved. " + req.getApplicantName() + " is now a Researcher."));
                } catch (ValidationException e) {
                    System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
                }
            }
            case "2" -> {
                System.out.print("  Reason (Enter to skip): ");
                String reason = ConsoleHelper.readChoice(scanner);
                if (reason.isEmpty()) reason = "No reason provided.";
                try {
                    rrs.rejectRequest(req.getRequestId(), reason);
                    messageService.send(manager.getId(), manager.getFullName(),
                            req.getApplicantId(),
                            "Your researcher role request has been REJECTED. Reason: " + reason);
                    DataRepository.getInstance().save();
                    System.out.println(Colors.yellow("  Rejected. Notification sent to " + req.getApplicantName() + "."));
                } catch (ValidationException e) {
                    System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
                }
            }
            default -> System.out.println(Colors.gray("  Cancelled."));
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void viewAllResearcherRequests(ResearcherRequestService rrs) {
        List<ResearcherRequest> all = new ArrayList<>(rrs.getAllRequests());
        if (all.isEmpty()) { System.out.println("  No researcher requests found."); return; }
        Paginator.viewList(all, "All Researcher Requests", ResearcherRequest::toString, scanner);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  9. ASSIGN RESEARCH SUPERVISOR
    // ══════════════════════════════════════════════════════════════════════════

    private void assignSupervisor() {
        // List 4th-year students
        List<Student> fourthYear = getAllStudents().stream()
                .filter(s -> s.getYear() == 4)
                .collect(Collectors.toList());

        if (fourthYear.isEmpty()) {
            System.out.println("  No 4th-year students found.");
            return;
        }

        System.out.println("\n  -- 4th-Year Students --");
        System.out.printf("  %-8s %-24s %s%n", "ID", "Name", "Current Supervisor");
        System.out.println("  " + "-".repeat(60));
        for (Student s : fourthYear) {
            String sup = s.getSupervisor() != null ? s.getSupervisor().getName() : "None";
            System.out.printf("  %-8s %-24s %s%n", s.getId(), s.getFullName(), sup);
        }

        System.out.print("  Student ID: ");
        String studentId = scanner.nextLine().trim();
        User u = authService.getUserById(studentId);
        if (!(u instanceof Student student) || student.getYear() != 4) {
            System.out.println("  [ERROR] Not a valid 4th-year student ID.");
            return;
        }

        // List available researchers
        List<User> researcherUsers = authService.getAllUsers().values().stream()
                .filter(user -> {
                    if (user instanceof Employee e) return e.isResearcher();
                    if (user instanceof Student s)  return s.isResearcher();
                    return false;
                })
                .collect(Collectors.toList());

        if (researcherUsers.isEmpty()) {
            System.out.println("  No researchers available in the system.");
            return;
        }

        System.out.println("\n  -- Available Researchers --");
        System.out.printf("  %-8s %-24s %s%n", "ID", "Name", "h-index");
        System.out.println("  " + "-".repeat(45));
        for (User r : researcherUsers) {
            ResearcherProfile rp = getProfile(r);
            if (rp != null)
                System.out.printf("  %-8s %-24s %d%n", r.getId(), r.getFullName(), rp.getHIndex());
        }

        System.out.print("  Researcher ID: ");
        String researcherId = scanner.nextLine().trim();
        if (studentId.equals(researcherId)) {
            System.out.println(Colors.red("  [ERROR] A student cannot be their own supervisor."));
            return;
        }
        User ru = authService.getUserById(researcherId);
        ResearcherProfile rp = getProfile(ru);

        if (rp == null) {
            System.out.println("  [ERROR] User is not a researcher.");
            return;
        }

        try {
            student.setSupervisor(rp);
            DataRepository.getInstance().save();
            System.out.println(Colors.green("  Supervisor assigned: " + ru.getFullName()
                    + " (h-index: " + rp.getHIndex() + ") -> " + student.getFullName()));
        } catch (HIndexException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        } catch (ValidationException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private ResearcherProfile getProfile(User u) {
        if (u instanceof Employee e) return e.getResearcherProfile();
        if (u instanceof Student s)  return s.getResearcherProfile();
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private void printMainMenu() {
        int regPending = registrationService.getPendingRequests().size();
        int resPending = ResearcherRequestService.getInstance().getPendingRequests().size();
        int unread     = messageService.getUnreadCount(manager.getId());

        System.out.println(Colors.gray("\n  =================================================="));
        System.out.println(Colors.bold("   Manager Menu - " + manager.getFullName()
                + "  [" + manager.getManagerType() + "]"));
        System.out.println(Colors.gray("  =================================================="));
        System.out.println("   1. Registration Requests"
                + (regPending > 0 ? Colors.yellow(" (" + regPending + " pending)") : ""));
        System.out.println("   2. Manage Courses");
        System.out.println("   3. View Students");
        System.out.println("   4. View Teachers");
        System.out.println("   5. Statistical Reports");
        System.out.println("   6. Manage News");
        System.out.println("   7. View Inbox" + (unread > 0 ? Colors.yellow(" [" + unread + " unread]") : ""));
        System.out.println("   8. Send Message");
        System.out.println("   9. Assign research supervisor (4th-year students)");
        System.out.println("   10. Researcher role requests"
                + (resPending > 0 ? Colors.yellow(" (" + resPending + " pending)") : ""));
        System.out.println("   0. Logout");
        System.out.print("  Choice: ");
    }

    private List<Student> getAllStudents() {
        return authService.getAllUsers().values().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .collect(Collectors.toList());
    }

    private List<Teacher> getAllTeachers() {
        return authService.getAllUsers().values().stream()
                .filter(u -> u instanceof Teacher)
                .map(u -> (Teacher) u)
                .collect(Collectors.toList());
    }

    /**
     * Prompts the user to pick a Faculty from the enum.
     *
     * @param allowNone if true, option 0 returns null (meaning "open to all")
     */
    private Faculty pickFaculty(String prompt, boolean allowNone) {
        Faculty[] values = Faculty.values();
        System.out.println("  " + (allowNone ? "0. Open to all faculties" : ""));
        for (int i = 0; i < values.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, values[i]);
        }
        System.out.print(prompt);
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (allowNone && idx == 0) return null;
            if (idx >= 1 && idx <= values.length) return values[idx - 1];
        } catch (NumberFormatException ignored) {}
        System.out.println("  [ERROR] Invalid selection.");
        return null;
    }
}
