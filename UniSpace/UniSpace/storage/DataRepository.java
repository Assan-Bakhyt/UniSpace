package UniSpace.storage;

import UniSpace.enums.Faculty;
import UniSpace.enums.ManagerType;
import UniSpace.enums.TeacherTitle;
import UniSpace.exception.CourseRegistrationException;
import UniSpace.model.course.Course;
import UniSpace.model.research.ResearchPaper;
import UniSpace.model.research.ResearchProject;
import UniSpace.model.research.ResearcherProfile;
import UniSpace.model.user.*;
import UniSpace.service.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Single source of truth for persistent data.
 * Pattern: Singleton.
 *
 * On first run: seeds demo accounts + courses, saves to disk.
 * On subsequent runs: loads from disk and restores all services.
 */
public class DataRepository {

    private static DataRepository instance;
    private static final String FILE = "UniSpace/resources/data/unispace_data.ser";

    private Map<String, User> users;

    private DataRepository() {
        DataStore store = FileStorage.load(FILE);
        if (store != null) {
            this.users = store.getUsers();
            System.err.println("[Storage] Loaded " + users.size() + " users from disk.");
            restoreServices(store);
        } else {
            this.users = new HashMap<>();
            seedDefaultData();
            save();
        }
        registerAllUsersForMessaging();
        // Inbox restore must happen AFTER observers are created in registerAllUsersForMessaging()
        if (store != null) restoreInboxes(store);
    }

    public static DataRepository getInstance() {
        if (instance == null) instance = new DataRepository();
        return instance;
    }

    // ── Data access ───────────────────────────────────────────────────────────

    public Map<String, User> getUsers() { return users; }

    // ── Save ──────────────────────────────────────────────────────────────────

    /**
     * Serializes the full system state to disk.
     * Called after every mutation (add/remove/update user, approve registration, etc.).
     */
    public void save() {
        CourseService       cs  = CourseService.getInstance();
        MarkService         ms  = MarkService.getInstance();
        NewsService         ns  = NewsService.getInstance();
        RegistrationService rs  = RegistrationService.getInstance();
        ComplaintService    cps = ComplaintService.getInstance();
        LogService          ls  = LogService.getInstance();

        ResearcherRequestService rrs = ResearcherRequestService.getInstance();
        MessageService           msg = MessageService.getInstance();

        FileStorage.save(new DataStore(
                users,
                cs.getCourseMap(),
                ms.getMarkStore(),
                cs.getStudentCourses(),
                cs.getStudentCredits(),
                cs.getFailCounts(),
                ns.getAllNewsRaw(),
                new java.util.ArrayList<>(rs.getAllRequests()),
                rrs.getRequestsForStore(),
                cps.getComplaintsForStore(),
                ls.getLogsForStore(),
                msg.getInboxMessages(),
                msg.getReadIndices()
        ), FILE);
    }

    // ── Restore ───────────────────────────────────────────────────────────────

    private void restoreServices(DataStore store) {
        CourseService cs = CourseService.getInstance();
        for (Course c : store.getCourses().values()) cs.addCourse(c);
        cs.restoreStudentData(
                store.getStudentCourses(),
                store.getStudentCredits(),
                store.getFailCounts()
        );

        MarkService.getInstance().restoreMarks(store.getMarks());
        NewsService.getInstance().restoreNews(store.getNewsList());
        RegistrationService.getInstance().restoreRequests(store.getRegistrationRequests());
        ResearcherRequestService.getInstance().restoreRequests(store.getResearcherRequests());
        ComplaintService.getInstance().restoreComplaints(store.getComplaints());
        LogService.getInstance().restoreLogs(store.getLogs());

        // Rebuild ResearchService from researcher profiles stored inside user objects
        ResearchService researchService = ResearchService.getInstance();
        for (User u : store.getUsers().values()) {
            UniSpace.model.research.ResearcherProfile rp = null;
            if (u instanceof Employee e) rp = e.getResearcherProfile();
            else if (u instanceof Student s) rp = s.getResearcherProfile();
            if (rp != null) {
                researchService.addResearcher(rp);
                for (UniSpace.model.research.ResearchProject p : rp.getResearchProjects()) {
                    researchService.addProject(p);
                }
            }
        }

        System.err.println("[Storage] Restored "
                + store.getCourses().size()   + " courses, "
                + store.getMarks().size()     + " marks, "
                + store.getNewsList().size()  + " news items.");
    }

    private void restoreInboxes(DataStore store) {
        if (store.getInboxMessages() == null) return;
        MessageService ms = MessageService.getInstance();
        store.getInboxMessages().forEach((userId, messages) -> {
            int readIdx = store.getInboxReadIndices() != null
                    ? store.getInboxReadIndices().getOrDefault(userId, messages.size())
                    : messages.size();
            ms.restoreUserInbox(userId, messages, readIdx);
        });
    }

    /** Ensures every user in the system is reachable via MessageService. */
    public void registerAllUsersForMessaging() {
        MessageService ms = MessageService.getInstance();
        for (User u : users.values()) {
            ms.register(u.getId(), u.getFullName());
        }
    }

    // ── Seed ──────────────────────────────────────────────────────────────────

    private void seedDefaultData() {
        // ── Users ─────────────────────────────────────────────────────────────
        Admin admin = new Admin("A001", "Alice", "Adminova",
                "admin@uni.kz", "admin123", Faculty.CS);

        Teacher professor = new Teacher("T001", "Bob", "Professorov",
                "teacher@uni.kz", "teacher123",
                Faculty.CS, 150_000.0, TeacherTitle.PROFESSOR);

        Teacher tutor = new Teacher("T002", "Tom", "Tutorov",
                "tutor@uni.kz", "tutor123",
                Faculty.MATH, 80_000.0, TeacherTitle.TUTOR);

        Student student4 = new Student("S001", "Carol", "Studentova",
                "student@uni.kz", "student123",
                4, Faculty.CS);

        Student student1 = new Student("S002", "Dan", "Freshman",
                "freshman@uni.kz", "fresh123",
                1, Faculty.MATH);

        Manager manager = new Manager("M001", "Eve", "Managersova",
                "manager@uni.kz", "manager123",
                Faculty.CS, 120_000.0, ManagerType.DEPARTMENT);

        for (User u : new User[]{admin, professor, tutor, student4, student1, manager}) {
            u.setPassword(UniSpace.util.Validator.hashPassword(u.getPassword()));
            users.put(u.getEmail(), u);
        }
        System.err.println("[Storage] Seeded " + users.size() + " default users.");

        // ── Courses ───────────────────────────────────────────────────────────
        CourseService cs = CourseService.getInstance();

        Course cs101 = new Course("CS101", "Introduction to Programming", 5);
        cs101.setTargetFaculty(Faculty.CS);
        cs101.setTargetYear(1);
        cs101.addInstructor("T001");
        cs.addCourse(cs101);

        Course cs301 = new Course("CS301", "Algorithms & Data Structures", 6);
        cs301.setTargetFaculty(Faculty.CS);
        cs301.setTargetYear(0); // open to all CS years
        cs301.addInstructor("T001");
        cs.addCourse(cs301);

        Course cs499 = new Course("CS499", "Research Methods", 5);
        cs499.setTargetFaculty(Faculty.CS);
        cs499.setTargetYear(4);
        cs499.addInstructor("T001");
        cs.addCourse(cs499);

        Course math101 = new Course("MATH101", "Calculus I", 5);
        math101.setTargetFaculty(Faculty.MATH);
        math101.setTargetYear(1);
        math101.addInstructor("T002");
        cs.addCourse(math101);

        Course math201 = new Course("MATH201", "Linear Algebra", 5);
        math201.setTargetFaculty(Faculty.MATH);
        math201.setTargetYear(2);
        math201.addInstructor("T002");
        cs.addCourse(math201);

        Course cs410 = new Course("CS410", "Machine Learning", 6);
        cs410.setTargetFaculty(Faculty.CS);
        cs410.setTargetYear(4);
        cs410.addInstructor("T001");
        cs.addCourse(cs410);

        // ── Enrolments ────────────────────────────────────────────────────────
        try {
            cs.registerStudentForCourse("S001", "CS499");
            cs.registerStudentForCourse("S001", "CS301");
            cs.registerStudentForCourse("S001", "CS410");
            cs.registerStudentForCourse("S002", "MATH101");
        } catch (CourseRegistrationException e) {
            System.err.println("[Seed] Enrolment error: " + e.getMessage());
        }

        // ── Marks ─────────────────────────────────────────────────────────────
        // att1/att2 max=30, finalExam max=40
        MarkService ms = MarkService.getInstance();
        try {
            ms.setAtt1("S001", "CS499", 25);
            ms.setAtt2("S001", "CS499", 26);
            ms.setFinalExam("S001", "CS499", 35); // total=86 -> B+ (3.33)
            ms.setAtt1("S001", "CS301", 22);
            ms.setAtt2("S001", "CS301", 20);      // total=42, admitted, final pending
            ms.setAtt1("S001", "CS410", 28);
            ms.setAtt2("S001", "CS410", 27);
            ms.setFinalExam("S001", "CS410", 38); // total=93 -> A- (3.67)
            ms.setAtt1("S002", "MATH101", 18);
            ms.setAtt2("S002", "MATH101", 15);    // total=33, admitted, final pending
        } catch (Exception e) {
            System.err.println("[Seed] Mark error: " + e.getMessage());
        }

        // ── Research — Professor Bob is PROFESSOR => always a researcher ──────
        professor.activateResearcher();
        ResearcherProfile bobProfile = professor.getResearcherProfile();

        ResearchPaper paper1 = new ResearchPaper();
        paper1.setName("Deep Learning for Anomaly Detection in Network Traffic");
        paper1.setJournal("IEEE Transactions on Neural Networks");
        paper1.setDoi("10.1109/tnn.2023.1234567");
        paper1.setPages(14);
        paper1.setCitations(87);
        paper1.setDate(java.time.LocalDate.of(2023, 3, 15));
        bobProfile.addResearchPaper(paper1);

        ResearchPaper paper2 = new ResearchPaper();
        paper2.setName("Federated Learning in Healthcare Systems");
        paper2.setJournal("Nature Machine Intelligence");
        paper2.setDoi("10.1038/s42256-022-00564-7");
        paper2.setPages(10);
        paper2.setCitations(142);
        paper2.setDate(java.time.LocalDate.of(2022, 9, 5));
        bobProfile.addResearchPaper(paper2);

        ResearchPaper paper3 = new ResearchPaper();
        paper3.setName("Transformer Architectures for Time-Series Forecasting");
        paper3.setJournal("Journal of Machine Learning Research");
        paper3.setDoi("10.5555/jmlr.2021.bob3");
        paper3.setPages(22);
        paper3.setCitations(54);
        paper3.setDate(java.time.LocalDate.of(2021, 6, 1));
        bobProfile.addResearchPaper(paper3);

        // h-index = 3: 3 papers each with >= 3 citations (87, 142, 54 — all qualify)
        bobProfile.setHIndex(3);

        ResearchProject project1 = new ResearchProject("AI in Medical Diagnostics");
        try { project1.joinProject(bobProfile); } catch (Exception ignored) {}
        project1.addPublishedPaper(paper1);
        project1.addPublishedPaper(paper2);
        bobProfile.addResearchProject(project1);

        // Carol (4th year) becomes researcher with her own paper
        student4.activateResearcher();
        ResearcherProfile carolProfile = student4.getResearcherProfile();

        ResearchPaper carolPaper = new ResearchPaper();
        carolPaper.setName("Survey of Explainable AI Methods");
        carolPaper.setJournal("ACM Computing Surveys");
        carolPaper.setDoi("10.1145/3560815");
        carolPaper.setPages(38);
        carolPaper.setCitations(23);
        carolPaper.setDate(java.time.LocalDate.of(2024, 1, 20));
        carolProfile.addResearchPaper(carolPaper);
        carolProfile.setHIndex(1);

        try { project1.joinProject(carolProfile); } catch (Exception ignored) {}
        carolProfile.addResearchProject(project1);

        ResearchProject project2 = new ResearchProject("Explainability in Deep Neural Networks");
        try { project2.joinProject(carolProfile); } catch (Exception ignored) {}
        carolProfile.addResearchProject(project2);

        // Assign Bob as Carol's research supervisor (h-index = 3 >= 3 required)
        try {
            student4.setSupervisor(bobProfile);
        } catch (Exception e) {
            System.err.println("[Seed] Supervisor error: " + e.getMessage());
        }

        // Register researchers and projects
        ResearchService rs = ResearchService.getInstance();
        rs.addResearcher(bobProfile);
        rs.addResearcher(carolProfile);
        rs.addProject(project1);
        rs.addProject(project2);

        // ── News ──────────────────────────────────────────────────────────────
        NewsService ns = NewsService.getInstance();
        ns.addNews("UniSpace 2.0 Launch",
                "We are excited to announce the launch of our new UniSpace platform! Enjoy improved navigation, color-coded menus, and full message persistence.",
                "M001", "Eve Managersova");
        ns.addNews("Research Grant Call — Spring 2026",
                "Applications for internal research grants are now open. Submit your proposal to the Research Office by May 30. Grants up to $5,000 per project.",
                "M001", "Eve Managersova");
        ns.addNews("Academic Year Closing — Important Dates",
                "Final exams run June 5-20. All marks must be submitted by June 25. Year promotion will be processed on June 30.",
                "M001", "Eve Managersova");

        System.err.println("[Storage] Seeded " + cs.getAllCourses().size() + " courses, research data and news.");
    }
}
