package UniSpace.model.course;

import java.util.Objects;

/**
 * Marks for one student in one course.
 *
 * Scoring structure:
 *   att1 (0–30) + att2 (0–30) → attestation total (max 60)
 *   finalExam (0–40)
 *   total = att1 + att2 + finalExam (max 100)
 *
 * Admission rule:
 *   att1 + att2 >= 30  → admitted to final exam
 *   att1 + att2 <  30  → course fail (retake whole course)
 *
 * Final exam outcomes (applies only when admitted):
 *   finalExam >= 20        → passed  (letter grade from total)
 *   10 <= finalExam < 20   → FX      (retake final exam only, not the course)
 *   finalExam < 10         → F       (retake whole course)
 *
 * Grade scale (when passed):
 *   95-100 → A  (4.00)   80-84 → B  (3.00)   65-69 → C  (2.00)   50-54 → D  (1.00)
 *   90-94  → A- (3.67)   75-79 → B- (2.67)   60-64 → C- (1.67)
 *   85-89  → B+ (3.33)   70-74 → C+ (2.33)   55-59 → D+ (1.33)
 */
public class Mark {

    private static final double ATT_MAX        = 30.0;
    private static final double FINAL_MAX      = 40.0;
    /** Minimum att1+att2 to be admitted to the final exam. */
    private static final double ATT_PASS_MIN   = 30.0;
    /** Minimum final exam score to pass. */
    private static final double FINAL_PASS_MIN = 20.0;
    /** Final scores in [FX_MIN, FINAL_PASS_MIN) result in FX (retake exam only). */
    private static final double FX_MIN         = 10.0;

    private String markId;
    private String studentId;
    private String courseCode;

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

    // ── Admission & outcome checks ────────────────────────────────────────────

    /** True if att1+att2 >= 30 (student may sit the final). */
    public boolean isAdmittedToFinal() {
        if (att1 == null || att2 == null) return false;
        return att1 + att2 >= ATT_PASS_MIN;
    }

    /** True if admitted AND finalExam >= 20. */
    public boolean isPassed() {
        return isAdmittedToFinal() && finalExam != null && finalExam >= FINAL_PASS_MIN;
    }

    /** True if admitted AND 10 <= finalExam < 20 — retake final exam only. */
    public boolean isFX() {
        return isAdmittedToFinal() && finalExam != null
                && finalExam >= FX_MIN && finalExam < FINAL_PASS_MIN;
    }

    /**
     * True if this counts as a course fail (must retake the whole course):
     * either att1+att2 < 30 (not admitted), or admitted but finalExam < 10.
     */
    public boolean isCourseFail() {
        if (att1 == null || att2 == null) return false;
        if (!isAdmittedToFinal()) return true;
        return finalExam != null && finalExam < FX_MIN;
    }

    // ── Calculated fields ─────────────────────────────────────────────────────

    /** Numeric total (0–100), or null if not all three components are set. */
    public Double getTotal() {
        if (att1 == null || att2 == null || finalExam == null) return null;
        return att1 + att2 + finalExam;
    }

    /** Letter grade. Returns "N/A" when scores are still incomplete. */
    public String getLetterGrade() {
        if (att1 == null || att2 == null) return "N/A";
        if (!isAdmittedToFinal())         return "F";
        if (finalExam == null)            return "N/A";
        if (isFX())                       return "FX";
        if (isCourseFail())               return "F";   // finalExam < 10
        // isPassed() is true here; total is guaranteed to be >= 50 (min 30+20)
        double t = getTotal();
        if (t >= 95) return "A";
        if (t >= 90) return "A-";
        if (t >= 85) return "B+";
        if (t >= 80) return "B";
        if (t >= 75) return "B-";
        if (t >= 70) return "C+";
        if (t >= 65) return "C";
        if (t >= 60) return "C-";
        if (t >= 55) return "D+";
        return "D";  // >= 50
    }

    /** GPA points on 4.0 scale. Returns 0.0 when not passed (F or FX). */
    public double getGradePoint() {
        if (!isPassed()) return 0.0;
        double t = getTotal();
        if (t >= 95) return 4.00;
        if (t >= 90) return 3.67;
        if (t >= 85) return 3.33;
        if (t >= 80) return 3.00;
        if (t >= 75) return 2.67;
        if (t >= 70) return 2.33;
        if (t >= 65) return 2.00;
        if (t >= 60) return 1.67;
        if (t >= 55) return 1.33;
        return 1.00;  // >= 50
    }

    /** Total as a primitive; returns 0.0 when not all components are set yet. */
    public double getTotalScore() {
        Double t = getTotal();
        return t != null ? t : 0.0;
    }

    // ── Setters with validation ───────────────────────────────────────────────

    public void setAtt1(double att1) {
        if (att1 < 0 || att1 > ATT_MAX)
            throw new IllegalArgumentException("att1 must be 0–" + ATT_MAX);
        this.att1 = att1;
    }

    public void setAtt2(double att2) {
        if (att2 < 0 || att2 > ATT_MAX)
            throw new IllegalArgumentException("att2 must be 0–" + ATT_MAX);
        this.att2 = att2;
    }

    public void setFinalExam(double finalExam) {
        if (finalExam < 0 || finalExam > FINAL_MAX)
            throw new IllegalArgumentException("finalExam must be 0–" + FINAL_MAX);
        this.finalExam = finalExam;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getMarkId()     { return markId; }
    public void   setMarkId(String id) { this.markId = id; }

    public String getStudentId()  { return studentId; }
    public void   setStudentId(String id) { this.studentId = id; }

    public String getCourseCode() { return courseCode; }
    public void   setCourseCode(String code) { this.courseCode = code; }

    public Double getAtt1()       { return att1; }
    public Double getAtt2()       { return att2; }
    public Double getFinalExam()  { return finalExam; }

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
        return Objects.equals(markId, ((Mark) o).markId);
    }

    @Override
    public int hashCode() { return Objects.hash(markId); }
}
