package UniSpace.model.course;

import java.util.Objects;

/**
 * Represents the marks for one student in one course.
 * Total = att1 (max 30) + att2 (max 30) + finalExam (max 40) = 100
 *
 * Grade scale:
 *   90-100 → A   (4.0)
 *   80-89  → B   (3.0)
 *   70-79  → C   (2.0)
 *   60-69  → D   (1.0)
 *   < 60   → F   (0.0)  ← counts as a fail
 */
public class Mark {
<<<<<<< HEAD

    private static final double ATT_MAX   = 30.0;
    private static final double FINAL_MAX = 40.0;
    private static final double PASS_MIN  = 50.0;

    private String  markId;
    private String  studentId;
    private String  courseCode;

    /** Attestation 1 score (0–30) */
    private Double att1;
    /** Attestation 2 score (0–30) */
    private Double att2;
    /** Final exam score (0–40) */
    private Double finalExam;

    public Mark() {}

    public Mark(String markId, String studentId, String courseCode) {
        this.markId     = markId;
        this.studentId  = studentId;
        this.courseCode = courseCode;
    }

    // ── Calculated fields ─────────────────────────────────────────────────────

    /**
     * Returns the numeric total (0–100), or null if not all components are set.
     */
    public Double getTotal() {
        if (att1 == null || att2 == null || finalExam == null) return null;
        return att1 + att2 + finalExam;
    }

    /**
     * Letter grade based on total. Returns "N/A" if total is not yet available.
     */
    public String getLetterGrade() {
        Double total = getTotal();
        if (total == null) return "N/A";
        if (total >= 95) return "A";
        if (total >= 90) return "A-";
        if (total >= 85) return "B+";
        if (total >= 80) return "B";
        if (total >= 75) return "C+";
        if (total >= 70) return "C";
        if (total >= 65) return "D+";
        if (total >= 60) return "D";
        return "F";
    }

    /**
     * GPA points (4-scale). Returns 0.0 if total is unavailable.
     */
    public double getGradePoint() {
        Double total = getTotal();
        if (total == null) return 0.0;
        if (total >= 95) return 4.0;
        if (total >= 90) return 3.67;
        if (total >= 85) return 3.33;
        if (total >= 80) return 3.0;
        if (total >= 75) return 2.33;
        if (total >= 70) return 2.0;
        if (total >= 65) return 1.33;
        if (total >= 60) return 1.0;
        return 0.0;
    }

    /**
     * Whether the student passed this course (total >= 50 and final >= 20).
     */
    public boolean isPassed() {
        Double total = getTotal();
        return total != null && total >= PASS_MIN && finalExam >= 20.0;
    }

    // ── Validation helpers ────────────────────────────────────────────────────

    public void setAtt1(double att1) {
        if (att1 < 0 || att1 > ATT_MAX)
            throw new IllegalArgumentException("att1 must be between 0 and " + ATT_MAX);
        this.att1 = att1;
    }

    public void setAtt2(double att2) {
        if (att2 < 0 || att2 > ATT_MAX)
            throw new IllegalArgumentException("att2 must be between 0 and " + ATT_MAX);
        this.att2 = att2;
    }

    public void setFinalExam(double finalExam) {
        if (finalExam < 0 || finalExam > FINAL_MAX)
            throw new IllegalArgumentException("finalExam must be between 0 and " + FINAL_MAX);
        this.finalExam = finalExam;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String  getMarkId()     { return markId; }
    public void    setMarkId(String id)  { this.markId = id; }

    public String  getStudentId()  { return studentId; }
    public void    setStudentId(String id) { this.studentId = id; }

    public String  getCourseCode() { return courseCode; }
    public void    setCourseCode(String code) { this.courseCode = code; }

    public Double  getAtt1()       { return att1; }
    public Double  getAtt2()       { return att2; }
    public Double  getFinalExam()  { return finalExam; }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format(
                "Mark{student='%s', course='%s', att1=%s, att2=%s, final=%s, total=%s, grade=%s}",
                studentId, courseCode, att1, att2, finalExam, getTotal(), getLetterGrade());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mark)) return false;
        Mark m = (Mark) o;
        return Objects.equals(markId, m.markId);
    }

    @Override
    public int hashCode() { return Objects.hash(markId); }
=======
    public double getTotalScore() {
        return 0.0; // Реализуйте!!!
    }
>>>>>>> origin/main
}
