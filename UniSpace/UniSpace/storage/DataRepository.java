package UniSpace.storage;

import UniSpace.enums.Faculty;
import UniSpace.enums.ManagerType;
import UniSpace.enums.TeacherTitle;
import UniSpace.model.user.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Single source of truth for persistent data.
 * Pattern: Singleton.
 *
 * On first run: seeds demo accounts and saves to disk.
 * On subsequent runs: loads from disk.
 */
public class DataRepository {

    private static DataRepository instance;
    private static final String FILE = "unispace_data.ser";

    private Map<String, User> users;

    private DataRepository() {
        DataStore store = FileStorage.load(FILE);
        if (store != null) {
            this.users = store.getUsers();
            System.out.println("[Storage] Loaded " + users.size() + " users from disk.");
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

    public void save() {
        FileStorage.save(new DataStore(users), FILE);
    }

    // ── Seed ──────────────────────────────────────────────────────────────────

    private void seedDefaultData() {
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
            users.put(u.getEmail(), u);
        }
        System.out.println("[Storage] Seeded " + users.size() + " default users.");
    }
}
