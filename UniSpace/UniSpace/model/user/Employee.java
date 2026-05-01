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

    private Faculty           department;
    private double            salary;

    // ── Researcher role (optional for any Employee) ───────────────────────────
    private boolean           isResearcher;
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

    public boolean isResearcher() { return isResearcher; }

    /**
     * Grants researcher role — creates a ResearcherProfile. Idempotent.
     */
    public void activateResearcher() {
        if (researcherProfile == null) researcherProfile = new ResearcherProfile(this);
        isResearcher = true;
    }

    /**
     * Enables or disables researcher status.
     * Subclasses may override to add restrictions (e.g., professors can't lose status).
     */
    public void setResearcher(boolean researcher) {
        this.isResearcher = researcher;
        if (!researcher) researcherProfile = null;
    }

    public ResearcherProfile getResearcherProfile() { return researcherProfile; }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return super.toString() + String.format(" | Dept: %s", department);
    }
}
