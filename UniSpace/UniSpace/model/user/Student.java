package UniSpace.model.user;

import UniSpace.enums.Faculty;
import UniSpace.enums.UserRole;
import UniSpace.exception.HIndexException;
import UniSpace.exception.ValidationException;
import UniSpace.model.research.ResearcherProfile;

public class Student extends User implements Comparable<Student> {

    public static final int MAX_CREDITS = 21;
    public static final int MAX_FAILS   = 3;

    private int     year;
    private Faculty faculty;

    private ResearcherProfile researcherProfile;
    private ResearcherProfile supervisor;

    public Student() {}

    public Student(String id, String firstName, String lastName,
                   String email, String password,
                   int year, Faculty faculty) {
        super(id, firstName, lastName, email, password, UserRole.STUDENT);
        setYear(year);
        this.faculty = faculty;
    }

    // ── Faculty ───────────────────────────────────────────────────────────────

    @Override
    public Faculty getFaculty() { return faculty; }

    // ── Researcher role ───────────────────────────────────────────────────────

    public boolean isResearcher() { return researcherProfile != null; }

    /**
     * Grants researcher role to this student — creates a ResearcherProfile
     * that holds hIndex and papers. Idempotent: safe to call multiple times.
     */
    public void activateResearcher() {
        if (researcherProfile == null) researcherProfile = new ResearcherProfile(this);
    }

    public ResearcherProfile getResearcherProfile() { return researcherProfile; }

    /**
     * Assigns a supervisor. Only 4th-year students may have one.
     * Supervisor must have h-index >= 3.
     *
     * @throws ValidationException if student is not in year 4
     * @throws HIndexException     if the supervisor's h-index is below 3
     */
    public void setSupervisor(ResearcherProfile supervisor) throws ValidationException, HIndexException {
        if (year != 4)
            throw new ValidationException("year",
                    "Only 4th-year students can have a research supervisor (current year: " + year + ")");
        if (supervisor.getHIndex() < 3)
            throw new HIndexException("Supervisor must have h-index >= 3, got: " + supervisor.getHIndex());
        this.supervisor = supervisor;
    }

    public ResearcherProfile getSupervisor() { return supervisor; }

    // ── Getters / setters ─────────────────────────────────────────────────────

    public int     getYear()             { return year; }
    public void    setYear(int year) {
        if (year < 1 || year > 4)
            throw new IllegalArgumentException("Student year must be 1–4, got: " + year);
        this.year = year;
    }
    public void    setFaculty(Faculty f) { this.faculty = f; }
    public double getGpa() {
        return UniSpace.service.MarkService.getInstance().getGpa(this.getId());
    }

    // ── Comparable: descending GPA ────────────────────────────────────────────

    @Override
    public int compareTo(Student other) {
        return Double.compare(other.getGpa(), this.getGpa());
    }

    @Override
    public String toString() {
        return super.toString() + String.format(
                " | Year: %d | Faculty: %s | GPA: %.2f", year, faculty, getGpa());
    }
}
