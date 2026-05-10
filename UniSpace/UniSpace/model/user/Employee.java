package UniSpace.model.user;

import UniSpace.enums.Faculty;
import UniSpace.enums.UserRole;
import UniSpace.model.research.ResearcherProfile;

import java.io.Serializable;

/**
 * Base class for all university employees (Teacher, Manager, Admin).
 * All employees can optionally be researchers — activate with activateResearcher().
 */
public abstract class Employee extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Faculty           department;
    private double            salary;

    // Было: два поля — boolean isResearcher + ResearcherProfile researcherProfile
    // Стало: только researcherProfile — истина определяется через != null
    private ResearcherProfile researcherProfile;

    public Employee() {}

    public Employee(String id, String firstName, String lastName,
                    String email, String password, UserRole role,
                    Faculty department, double salary) {
        super(id, firstName, lastName, email, password, role);
        this.department = department;
        this.salary     = salary;
    }

    // ── Faculty ───────────────────────────────────────────────────────────────

    @Override
    public Faculty getFaculty() { return department; }

    public Faculty getDepartment()             { return department; }
    public void    setDepartment(Faculty dept) { this.department = dept; }
    public double  getSalary()                 { return salary; }
    public void    setSalary(double salary)    { this.salary = salary; }

    // ── Researcher role ───────────────────────────────────────────────────────

    /**
     * Было return isResearcher;
     * Стало: определяется наличием профиля — один источник правды.
     */
    public boolean isResearcher() {
        return researcherProfile != null;
    }

    /**
     * Grants researcher role — creates a ResearcherProfile. Idempotent.
     */
    public void activateResearcher() {
        if (researcherProfile == null) researcherProfile = new ResearcherProfile(this);
    }

    /**
     * Enables or disables researcher status.
     * Subclasses may override to add restrictions (e.g., professors can't lose status).
     */
    public void setResearcher(boolean researcher) {
        if (researcher) {
            activateResearcher(); // создаёт профиль если нет
        } else {
            researcherProfile = null; // удаляет профиль
        }
    }

    public ResearcherProfile getResearcherProfile() { return researcherProfile; }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return super.toString() + String.format(" | Dept: %s", department);
    }
}