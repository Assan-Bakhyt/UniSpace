package UniSpace.patterns;

import UniSpace.enums.Faculty;
import UniSpace.enums.ManagerType;
import UniSpace.enums.TeacherTitle;
import UniSpace.model.user.*;

import java.util.UUID;

/**
 * Creates User subclass instances with auto-generated IDs.
 * Pattern: Factory Method (GoF).
 *
 * Centralises object construction so callers don't depend on concrete
 * constructors and all IDs are generated consistently.
 */
public class UserFactory {

    private UserFactory() {}

    public static Student createStudent(String firstName, String lastName,
                                        String email, String password,
                                        int year, Faculty faculty) {
        return new Student(newId("S"), firstName, lastName, email, password, year, faculty);
    }

    public static Teacher createTeacher(String firstName, String lastName,
                                        String email, String password,
                                        Faculty department, double salary,
                                        TeacherTitle title) {
        return new Teacher(newId("T"), firstName, lastName, email, password,
                department, salary, title);
    }

    public static Manager createManager(String firstName, String lastName,
                                        String email, String password,
                                        Faculty department, double salary,
                                        ManagerType managerType) {
        return new Manager(newId("M"), firstName, lastName, email, password,
                department, salary, managerType);
    }

    public static Admin createAdmin(String firstName, String lastName,
                                    String email, String password,
                                    Faculty department) {
        return new Admin(newId("A"), firstName, lastName, email, password, department);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static String newId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
