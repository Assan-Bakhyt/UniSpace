package UniSpace.ui;

import UniSpace.enums.Faculty;
import UniSpace.exception.CourseRegistrationException;
import UniSpace.exception.HIndexException;
import UniSpace.exception.ValidationException;
import UniSpace.model.course.Course;
import UniSpace.model.course.RegistrationRequest;
import UniSpace.model.news.News;
import UniSpace.model.research.ResearcherProfile;
import UniSpace.model.user.Employee;
import UniSpace.model.user.Manager;
import UniSpace.model.user.Student;
import UniSpace.model.user.Teacher;
import UniSpace.model.user.User;
import UniSpace.service.*;
import UniSpace.storage.DataRepository;

import java.util.*;
import java.util.stream.Collectors;

public class ManagerMenu {

    private final Manager             manager;
    private final CourseService       courseService;
    private final MarkService         markService;
    private final RegistrationService registrationService;
    private final NewsService         newsService;
    private final MessageService      messageService;
    private final AuthService         authService;
    private final Scanner             scanner;

    public ManagerMenu(Manager manager,
                       CourseService courseService,
                       MarkService markService,
                       RegistrationService registrationService,
                       NewsService newsService,
                       MessageService messageService) {
        this.manager             = manager;
        this.courseService       = courseService;
        this.markService         = markService;
        this.registrationService = registrationService;
        this.newsService         = newsService;
        this.messageService      = messageService;
        this.authService         = AuthService.getInstance();
        this.scanner             = new Scanner(System.in);
    }

    // ── Main loop ─────────────────────────────────────────────────────────────

    public void show() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> manageRegistrationRequests();
                case "2" -> manageCourses();
                case "3" -> viewStudents();
                case "4" -> viewTeachers();
                case "5" -> statisticalReports();
                case "6" -> manageNews();
                case "7" -> viewInbox();
                case "8" -> sendMessage();
                case "9" -> assignSupervisor();
                case "0" -> running = false;
                default  -> System.out.println("  Invalid option.");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  1. REGISTRATION REQUESTS
    // ══════════════════════════════════════════════════════════════════════════

    private void manageRegistrationRequests() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ── Registration Requests ──");
            System.out.println("   1. View pending requests");
            System.out.println("   2. Approve request");
            System.out.println("   3. Reject request");
            System.out.println("   4. View all requests (history)");
            System.out.println("   0. Back");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> viewPendingRequests();
                case "2" -> approveRequest();
                case "3" -> rejectRequest();
                case "4" -> viewAllRequests();
                case "0" -> back = true;
                default  -> System.out.println("  Invalid option.");
            }
        }
    }

    private void viewPendingRequests() {
        List<RegistrationRequest> pending = registrationService.getPendingRequests();
        if (pending.isEmpty()) { System.out.println("  No pending requests."); return; }
        System.out.println("\n  Pending requests (" + pending.size() + "):");
        pending.forEach(r -> System.out.println("  " + r));
    }

    private void approveRequest() {
        viewPendingRequests();
        System.out.print("  Request ID to approve: ");
        String id = scanner.nextLine().trim();
        try {
            RegistrationRequest req = registrationService.approveRequest(id, courseService);
            DataRepository.getInstance().save();
            System.out.println("  Approved: " + req.getStudentId() + " → " + req.getCourseCode());
        } catch (CourseRegistrationException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void rejectRequest() {
        viewPendingRequests();
        System.out.print("  Request ID to reject: ");
        String id = scanner.nextLine().trim();
        System.out.print("  Reason (optional): ");
        String reason = scanner.nextLine().trim();
        try {
            registrationService.rejectRequest(id, reason.isEmpty() ? null : reason);
            DataRepository.getInstance().save();
            System.out.println("  Request rejected.");
        } catch (CourseRegistrationException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void viewAllRequests() {
        List<RegistrationRequest> all = new ArrayList<>(registrationService.getAllRequests());
        if (all.isEmpty()) { System.out.println("  No requests found."); return; }
        System.out.println("\n  All requests (" + all.size() + "):");
        all.forEach(r -> System.out.println("  " + r));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  2. MANAGE COURSES
    // ══════════════════════════════════════════════════════════════════════════

    private void manageCourses() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ── Manage Courses ──");
            System.out.println("   1. Add new course");
            System.out.println("   2. View all courses");
            System.out.println("   3. Assign teacher to course");
            System.out.println("   4. Course details");
            System.out.println("   0. Back");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> addCourse();
                case "2" -> viewAllCourses();
                case "3" -> assignTeacher();
                case "4" -> viewCourseDetails();
                case "0" -> back = true;
                default  -> System.out.println("  Invalid option.");
            }
        }
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
        System.out.println("  Course added: " + course.getCourseCode());
    }

    private void viewAllCourses() {
        Collection<Course> courses = courseService.getAllCourses();
        if (courses.isEmpty()) { System.out.println("  No courses found."); return; }
        System.out.println("\n  ── All Courses ──");
        System.out.printf("  %-8s %-32s %-8s %-24s %s%n",
                "Code", "Name", "Credits", "Faculty", "Year");
        System.out.println("  " + "─".repeat(85));
        for (Course c : courses) {
            String fac  = c.getTargetFaculty() != null ? c.getTargetFaculty().toString() : "All";
            String year = c.getTargetYear() > 0 ? "Y" + c.getTargetYear() : "All";
            System.out.printf("  %-8s %-32s %-8d %-24s %s%n",
                    c.getCourseCode(), c.getName(), c.getCredits(), fac, year);
        }
    }

    private void assignTeacher() {
        viewAllCourses();
        System.out.print("  Course code: ");
        String code = scanner.nextLine().trim();
        System.out.print("  Teacher ID: ");
        String teacherId = scanner.nextLine().trim();

        User u = authService.getUserById(teacherId);
        if (!(u instanceof Teacher)) {
            System.out.println("  [ERROR] No teacher found with ID: " + teacherId);
            return;
        }
        try {
            courseService.assignInstructor(code, teacherId);
            ((Teacher) u).addCourse(courseService.findCourse(code).orElse(null));
            DataRepository.getInstance().save();
            System.out.println("  Teacher " + u.getFullName() + " assigned to " + code);
        } catch (CourseRegistrationException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void viewCourseDetails() {
        System.out.print("  Course code: ");
        String code = scanner.nextLine().trim();
        courseService.findCourse(code).ifPresentOrElse(c -> {
            System.out.println("\n  ── Course Details ──");
            System.out.println("  Code    : " + c.getCourseCode());
            System.out.println("  Name    : " + c.getName());
            System.out.println("  Credits : " + c.getCredits());
            System.out.println("  Faculty : " + (c.getTargetFaculty() != null ? c.getTargetFaculty() : "Open to all"));
            System.out.println("  Year    : " + (c.getTargetYear() > 0 ? "Y" + c.getTargetYear() : "Open to all"));
            System.out.println("  Teachers: " + c.getInstructorIds());
            System.out.println("  Lessons : " + c.getLessons().size());
        }, () -> System.out.println("  Course not found: " + code));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  3. VIEW STUDENTS
    // ══════════════════════════════════════════════════════════════════════════

    private void viewStudents() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ── View Students ──");
            System.out.println("   1. All students by GPA (descending)");
            System.out.println("   2. All students alphabetically");
            System.out.println("   3. Students by faculty");
            System.out.println("   4. Student details");
            System.out.println("   0. Back");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> listStudents(Comparator.comparingDouble(
                        s -> -markService.getGpa(s.getId())));
                case "2" -> listStudents(Comparator.<Student, String>comparing(User::getLastName)
                        .thenComparing(User::getFirstName));
                case "3" -> listStudentsByFaculty();
                case "4" -> viewStudentDetails();
                case "0" -> back = true;
                default  -> System.out.println("  Invalid option.");
            }
        }
    }

    private void listStudents(Comparator<Student> comparator) {
        List<Student> students = getAllStudents();
        if (students.isEmpty()) { System.out.println("  No students found."); return; }
        students.sort(comparator);
        printStudentTable(students);
    }

    private void listStudentsByFaculty() {
        Faculty f = pickFaculty("  Faculty: ", false);
        if (f == null) return;
        List<Student> students = getAllStudents().stream()
                .filter(s -> f == s.getFaculty())
                .sorted(Comparator.comparing(User::getLastName))
                .collect(Collectors.toList());
        if (students.isEmpty()) { System.out.println("  No students in " + f); return; }
        printStudentTable(students);
    }

    private void viewStudentDetails() {
        System.out.print("  Student ID: ");
        String id = scanner.nextLine().trim();
        User u = authService.getUserById(id);
        if (!(u instanceof Student s)) { System.out.println("  Student not found."); return; }
        System.out.println("\n  ── Student Details ──");
        System.out.println("  " + s);
        System.out.println("  Credits enrolled : " + courseService.getStudentCredits(s.getId()));
        System.out.println("  Cumulative GPA   : " + String.format("%.2f", markService.getGpa(s.getId())));
        System.out.println("\n" + markService.generateTranscript(s.getId()));
    }

    private void printStudentTable(List<Student> students) {
        System.out.println("\n  ── Students ──");
        System.out.printf("  %-8s %-22s %-6s %-24s %s%n",
                "ID", "Name", "Year", "Faculty", "GPA");
        System.out.println("  " + "─".repeat(75));
        for (Student s : students) {
            double gpa = markService.getGpa(s.getId());
            System.out.printf("  %-8s %-22s %-6d %-24s %.2f%n",
                    s.getId(), s.getFullName(), s.getYear(),
                    s.getFaculty(), gpa);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  4. VIEW TEACHERS
    // ══════════════════════════════════════════════════════════════════════════

    private void viewTeachers() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ── View Teachers ──");
            System.out.println("   1. By rating (descending)");
            System.out.println("   2. Alphabetically");
            System.out.println("   3. Teacher details");
            System.out.println("   0. Back");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> listTeachers(Comparator.comparingDouble(
                        (Teacher t) -> t.getRating()).reversed());
                case "2" -> listTeachers(Comparator.<Teacher, String>comparing(User::getLastName)
                        .thenComparing(User::getFirstName));
                case "3" -> viewTeacherDetails();
                case "0" -> back = true;
                default  -> System.out.println("  Invalid option.");
            }
        }
    }

    private void listTeachers(Comparator<Teacher> comparator) {
        List<Teacher> teachers = getAllTeachers();
        if (teachers.isEmpty()) { System.out.println("  No teachers found."); return; }
        teachers.sort(comparator);
        System.out.println("\n  ── Teachers ──");
        System.out.printf("  %-8s %-22s %-16s %-7s %-10s%n",
                "ID", "Name", "Title", "Rating", "Researcher");
        System.out.println("  " + "─".repeat(70));
        for (Teacher t : teachers) {
            System.out.printf("  %-8s %-22s %-16s %-7.1f %s%n",
                    t.getId(), t.getFullName(), t.getTitle(),
                    t.getRating(), t.isResearcher() ? "Yes" : "No");
        }
    }

    private void viewTeacherDetails() {
        System.out.print("  Teacher ID: ");
        String id = scanner.nextLine().trim();
        User u = authService.getUserById(id);
        if (!(u instanceof Teacher t)) { System.out.println("  Teacher not found."); return; }
        System.out.println("\n  ── Teacher Details ──");
        System.out.println("  " + t);
        List<Course> courses = courseService.getCoursesByInstructor(t.getId());
        System.out.println("  Courses (" + courses.size() + "): "
                + courses.stream().map(Course::getCourseCode).collect(Collectors.joining(", ")));
        if (t.isResearcher()) {
            System.out.println("  Researcher: YES | Papers: "
                    + t.getResearcherProfile().getResearchPapers().size()
                    + " | h-index: " + t.getResearcherProfile().getHIndex());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  5. STATISTICAL REPORTS
    // ══════════════════════════════════════════════════════════════════════════

    private void statisticalReports() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ── Statistical Reports ──");
            System.out.println("   1. Mark report for a course");
            System.out.println("   2. GPA distribution (all students)");
            System.out.println("   3. Top-N students by GPA");
            System.out.println("   4. Students with at least one failed course");
            System.out.println("   0. Back");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> courseMarkReport();
                case "2" -> gpaDistribution();
                case "3" -> topStudentsByGpa();
                case "4" -> studentsWithFails();
                case "0" -> back = true;
                default  -> System.out.println("  Invalid option.");
            }
        }
    }

    private void courseMarkReport() {
        System.out.print("  Course code: ");
        System.out.println(markService.generateMarkReport(scanner.nextLine().trim()));
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
        System.out.println("\n  ── GPA Distribution ──");
        System.out.printf("  A  (≥ 3.67) : %d students%n", count4);
        System.out.printf("  B  (3.00–3.66): %d students%n", count3);
        System.out.printf("  C  (2.00–2.99): %d students%n", count2);
        System.out.printf("  D  (0.01–1.99): %d students%n", count1);
        System.out.printf("  No grades yet : %d students%n", count0);
        System.out.printf("  Total         : %d students%n", students.size());
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

        System.out.println("\n  ── Top " + n + " Students ──");
        int rank = 1;
        for (Student s : sorted) {
            System.out.printf("  %2d. %-22s GPA: %.2f  %s Y%d%n",
                    rank++, s.getFullName(), markService.getGpa(s.getId()),
                    s.getFaculty(), s.getYear());
        }
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
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  6. MANAGE NEWS
    // ══════════════════════════════════════════════════════════════════════════

    private void manageNews() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ── Manage News ──");
            System.out.println("   1. Add news");
            System.out.println("   2. View all news");
            System.out.println("   3. Remove news");
            System.out.println("   0. Back");
            System.out.print("  Choice: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> addNews();
                case "2" -> viewAllNews();
                case "3" -> removeNews();
                case "0" -> back = true;
                default  -> System.out.println("  Invalid option.");
            }
        }
    }

    private void addNews() {
        System.out.print("  Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("  Content: ");
        String content = scanner.nextLine().trim();
        if (title.isEmpty()) { System.out.println("  [ERROR] Title cannot be empty."); return; }
        News news = newsService.addNews(title, content, manager.getId(), manager.getFullName());
        DataRepository.getInstance().save();
        System.out.println("  News published: " + news);
    }

    private void viewAllNews() {
        List<News> news = newsService.getAllNews();
        if (news.isEmpty()) { System.out.println("  No news published yet."); return; }
        System.out.println("\n  ── News ──");
        for (News n : news) {
            System.out.println("  ID: " + n.getNewsId().substring(0, 8) + "...");
            System.out.println("  " + n);
            if (n.getContent() != null && !n.getContent().isEmpty())
                System.out.println("  " + n.getContent());
            System.out.println();
        }
    }

    private void removeNews() {
        viewAllNews();
        System.out.print("  News ID (first 8 chars): ");
        String prefix = scanner.nextLine().trim();
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

    private void viewInbox() {
        List<String> messages = messageService.getInbox(manager.getId());
        if (messages.isEmpty()) { System.out.println("  Inbox is empty."); return; }
        System.out.println("\n  ── Inbox ──");
        messages.forEach(m -> System.out.println("  " + m));
    }

    private void sendMessage() {
        System.out.print("  Recipient user ID: ");
        String toId = scanner.nextLine().trim();
        System.out.print("  Message: ");
        String text = scanner.nextLine().trim();
        boolean sent = messageService.send(manager.getId(), manager.getFullName(), toId, text);
        System.out.println(sent ? "  Message sent." : "  [ERROR] Recipient not found or not online.");
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

        System.out.println("\n  ── 4th-Year Students ──");
        System.out.printf("  %-8s %-24s %s%n", "ID", "Name", "Current Supervisor");
        System.out.println("  " + "─".repeat(60));
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

        System.out.println("\n  ── Available Researchers ──");
        System.out.printf("  %-8s %-24s %s%n", "ID", "Name", "h-index");
        System.out.println("  " + "─".repeat(45));
        for (User r : researcherUsers) {
            ResearcherProfile rp = getProfile(r);
            if (rp != null)
                System.out.printf("  %-8s %-24s %d%n", r.getId(), r.getFullName(), rp.getHIndex());
        }

        System.out.print("  Researcher ID: ");
        String researcherId = scanner.nextLine().trim();
        User ru = authService.getUserById(researcherId);
        ResearcherProfile rp = getProfile(ru);

        if (rp == null) {
            System.out.println("  [ERROR] User is not a researcher.");
            return;
        }

        try {
            student.setSupervisor(rp);
            DataRepository.getInstance().save();
            System.out.println("  Supervisor assigned: " + ru.getFullName()
                    + " (h-index: " + rp.getHIndex() + ") → " + student.getFullName());
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
        System.out.println("\n  ══════════════════════════════════════════════════");
        System.out.println("   Manager Menu — " + manager.getFullName()
                + "  [" + manager.getManagerType() + "]");
        System.out.println("  ══════════════════════════════════════════════════");
        int pending = registrationService.getPendingRequests().size();
        System.out.println("   1. Registration Requests"
                + (pending > 0 ? " (" + pending + " pending)" : ""));
        System.out.println("   2. Manage Courses");
        System.out.println("   3. View Students");
        System.out.println("   4. View Teachers");
        System.out.println("   5. Statistical Reports");
        System.out.println("   6. Manage News");
        System.out.println("   7. View Inbox"
                + (messageService.hasMessages(manager.getId()) ? " (new)" : ""));
        System.out.println("   8. Send Message");
        System.out.println("   9. Assign research supervisor (4th-year students)");
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
