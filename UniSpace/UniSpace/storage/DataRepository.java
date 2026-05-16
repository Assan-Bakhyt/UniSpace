package UniSpace.storage;

import UniSpace.enums.Faculty;
import UniSpace.enums.ManagerType;
import UniSpace.enums.TeacherTitle;
import UniSpace.exception.CourseRegistrationException;
import UniSpace.model.course.Course;
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
            System.out.println("[Storage] Loaded " + users.size() + " users from disk.");
            restoreServices(store);
        } else {
            this.users = new HashMap<>();
            seedDefaultData();
            save();
        }
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

        FileStorage.save(new DataStore(
                users,
                cs.getCourseMap(),
                ms.getMarkStore(),
                cs.getStudentCourses(),
                cs.getStudentCredits(),
                cs.getFailCounts(),
                ns.getAllNewsRaw(),
                new java.util.ArrayList<>(rs.getAllRequests()),
                cps.getComplaintsForStore(),
                ls.getLogsForStore()
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

        System.out.println("[Storage] Restored "
                + store.getCourses().size()   + " courses, "
                + store.getMarks().size()     + " marks, "
                + store.getNewsList().size()  + " news items.");
    }

    // ── Seed ──────────────────────────────────────────────────────────────────

    private void seedDefaultData() {
        // ── Users ────────────────────────────────────────────────────────────
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
        System.out.println("[Storage] Seeded " + users.size() + " default users.");

        // ── Courses ───────────────────────────────────────────────────────────
        CourseService cs = CourseService.getInstance();

        Course cs101 = new Course("CS101", "Introduction to Programming", 5);
        cs101.setTargetFaculty(Faculty.CS);
        cs101.setTargetYear(1);
        cs101.addInstructor("T001");
        cs.addCourse(cs101);

        Course cs301 = new Course("CS301", "Algorithms & Data Structures", 6);
        cs301.setTargetFaculty(Faculty.CS);
        cs301.setTargetYear(3);
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

        // ── Sample enrolments ─────────────────────────────────────────────────
        try {
            cs.registerStudentForCourse("S001", "CS499");
            cs.registerStudentForCourse("S002", "MATH101");
        } catch (CourseRegistrationException e) {
            System.err.println("[Seed] Enrolment error: " + e.getMessage());
        }

        System.out.println("[Storage] Seeded " + cs.getAllCourses().size() + " default courses.");
    }
}
