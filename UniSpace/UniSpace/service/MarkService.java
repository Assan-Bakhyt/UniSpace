package UniSpace.service;

import UniSpace.model.course.Course;
import UniSpace.model.course.Mark;

import java.util.*;

/**
 * Manages grade entry and transcript generation.
 */
public class MarkService {

    private static MarkService instance;

    public static MarkService getInstance() {
        if (instance == null) instance = new MarkService(CourseService.getInstance());
        return instance;
    }

    /** "studentId:courseCode" → Mark */
    private final Map<String, Mark> markStore = new HashMap<>();

    private final CourseService courseService;

    public MarkService(CourseService courseService) {
        this.courseService = courseService;
    }

    // ── Grade entry ───────────────────────────────────────────────────────────

    /**
     * Creates or retrieves the Mark record for a student/course pair.
     */
    public Mark getOrCreateMark(String studentId, String courseCode) {
        String key = key(studentId, courseCode);
        return markStore.computeIfAbsent(key,
                k -> new Mark(UUID.randomUUID().toString(), studentId, courseCode));
    }

    /**
     * Sets att1 score (0–30) for a student in a course.
     */
    public void setAtt1(String studentId, String courseCode, double score) {
        getOrCreateMark(studentId, courseCode).setAtt1(score);
    }

    /**
     * Sets att2 score (0–30) for a student in a course.
     * If att1 is already set and att1+att2 < 30, records a course fail immediately —
     * the student is not admitted to the final exam.
     */
    public void setAtt2(String studentId, String courseCode, double score) {
        Mark mark = getOrCreateMark(studentId, courseCode);
        mark.setAtt2(score);
        if (mark.getAtt1() != null && mark.isCourseFail()) {
            courseService.recordFail(studentId, courseCode);
        }
    }

    /**
     * Sets final exam score (0–40) for a student in a course.
     *
     * Outcomes recorded automatically:
     *   finalExam >= 20        → passed (no fail recorded)
     *   10 <= finalExam < 20   → FX, retake exam only (no course fail)
     *   finalExam < 10         → F, course fail recorded
     *
     * If the student was not admitted (att1+att2 < 30), the fail was already
     * recorded in setAtt2() and is not double-counted here.
     */
    public void setFinalExam(String studentId, String courseCode, double score) {
        Mark mark = getOrCreateMark(studentId, courseCode);
        mark.setFinalExam(score);
        // Record course fail only when admitted but final < 10 (FX is NOT a course fail)
        if (mark.isAdmittedToFinal() && mark.isCourseFail()) {
            courseService.recordFail(studentId, courseCode);
        }
    }

    /**
     * Returns the Mark for a student/course, or empty if not found.
     */
    public Optional<Mark> getMark(String studentId, String courseCode) {
        return Optional.ofNullable(markStore.get(key(studentId, courseCode)));
    }

    /**
     * Returns all Mark records for a student (across all courses).
     */
    public List<Mark> getMarksForStudent(String studentId) {
        List<Mark> result = new ArrayList<>();
        for (Map.Entry<String, Mark> entry : markStore.entrySet()) {
            if (entry.getKey().startsWith(studentId + ":"))
                result.add(entry.getValue());
        }
        return result;
    }

    /**
     * Returns all Mark records for a given course (all students).
     */
    public List<Mark> getMarksForCourse(String courseCode) {
        List<Mark> result = new ArrayList<>();
        for (Mark m : markStore.values()) {
            if (m.getCourseCode().equals(courseCode))
                result.add(m);
        }
        return result;
    }

    // ── Transcript ────────────────────────────────────────────────────────────

    /**
     * Generates a plain-text academic transcript for a student.
     *
     * Format:
     * ============================================================
     *  ACADEMIC TRANSCRIPT — Student ID: <id>
     * ============================================================
     *  Course       Credits  Att1  Att2  Final  Total  Grade  GPA
     *  ...
     * ------------------------------------------------------------
     *  Cumulative GPA: X.XX   Total credits attempted: XX
     * ============================================================
     */
    public String generateTranscript(String studentId) {
        List<Course> courses = courseService.getStudentCourses(studentId);
        List<Mark>   marks   = getMarksForStudent(studentId);

        // index marks by courseCode for quick lookup
        Map<String, Mark> markIndex = new HashMap<>();
        for (Mark m : marks) markIndex.put(m.getCourseCode(), m);

        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append(String.format(" ACADEMIC TRANSCRIPT — Student ID: %s%n", studentId));
        sb.append("============================================================\n");
        sb.append(String.format("%-12s %-8s %-6s %-6s %-7s %-7s %-6s %-4s%n",
                "Course", "Credits", "Att1", "Att2", "Final", "Total", "Grade", "GPA"));
        sb.append("------------------------------------------------------------\n");

        double totalGpaPoints   = 0.0;
        int    totalCredits     = 0;
        int    gradedCourses    = 0;

        for (Course course : courses) {
            Mark mark = markIndex.get(course.getCourseCode());
            String att1  = mark != null && mark.getAtt1()      != null ? String.format("%.1f", mark.getAtt1())      : "-";
            String att2  = mark != null && mark.getAtt2()      != null ? String.format("%.1f", mark.getAtt2())      : "-";
            String fin   = mark != null && mark.getFinalExam() != null ? String.format("%.1f", mark.getFinalExam()) : "-";
            String total = mark != null && mark.getTotal()     != null ? String.format("%.1f", mark.getTotal())     : "-";
            String grade = mark != null ? mark.getLetterGrade() : "N/A";
            String gpa   = mark != null && mark.getTotal() != null
                    ? String.format("%.1f", mark.getGradePoint()) : "-";

            sb.append(String.format("%-12s %-8d %-6s %-6s %-7s %-7s %-6s %-4s%n",
                    course.getCourseCode(), course.getCredits(),
                    att1, att2, fin, total, grade, gpa));

            if (mark != null && mark.getTotal() != null) {
                totalGpaPoints += mark.getGradePoint() * course.getCredits();
                totalCredits   += course.getCredits();
                gradedCourses++;
            }
        }

        sb.append("------------------------------------------------------------\n");
        double cumulativeGpa = totalCredits > 0 ? totalGpaPoints / totalCredits : 0.0;
        sb.append(String.format(" Cumulative GPA: %.2f   Total credits attempted: %d%n",
                cumulativeGpa, courseService.getStudentCredits(studentId)));
        sb.append("============================================================\n");
        return sb.toString();
    }

    /**
     * Calculates the cumulative GPA for a student based on all graded courses.
     * Weighted by credits: sum(gradePoint * credits) / sum(credits).
     */
    public double getGpa(String studentId) {
        List<Course> courses = courseService.getStudentCourses(studentId);
        Map<String, Mark> markIndex = new HashMap<>();
        for (Mark m : getMarksForStudent(studentId)) markIndex.put(m.getCourseCode(), m);

        double totalGpaPoints = 0.0;
        int    totalCredits   = 0;
        for (Course course : courses) {
            Mark mark = markIndex.get(course.getCourseCode());
            if (mark != null && mark.getTotal() != null) {
                totalGpaPoints += mark.getGradePoint() * course.getCredits();
                totalCredits   += course.getCredits();
            }
        }
        return totalCredits > 0 ? totalGpaPoints / totalCredits : 0.0;
    }

    // ── Mark Report (for Teacher) ─────────────────────────────────────────────

    /**
     * Generates a mark report for a course — used by teachers.
     *
     * Shows every student's scores + statistics:
     * average total, grade distribution (A/B/C/D/F), pass rate.
     *
     * Format:
     * ============================================================
     *  MARK REPORT — Course: <code>
     * ============================================================
     *  Student      Att1   Att2   Final  Total  Grade
     *  ...
     * ------------------------------------------------------------
     *  Students: 20   Passed: 17   Failed: 3   Pass rate: 85%
     *  Average total: 74.3
     *  Grade distribution: A=3  B=6  C=5  D=3  F=3
     * ============================================================
     */
    public String generateMarkReport(String courseCode) {
        List<Mark> marks = getMarksForCourse(courseCode);

        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append(String.format(" MARK REPORT — Course: %s%n", courseCode));
        sb.append("============================================================\n");

        if (marks.isEmpty()) {
            sb.append(" No marks recorded for this course yet.\n");
            sb.append("============================================================\n");
            return sb.toString();
        }

        sb.append(String.format("%-14s %-7s %-7s %-7s %-7s %-6s%n",
                "Student", "Att1", "Att2", "Final", "Total", "Grade"));
        sb.append("------------------------------------------------------------\n");

        // stats counters
        int totalStudents = marks.size();
        int passed        = 0;
        int fx            = 0;
        int failed        = 0;
        double sumTotal   = 0.0;
        int gradedCount   = 0;
        Map<String, Integer> gradeDist = new LinkedHashMap<>();
        for (String g : new String[]{"A","A-","B+","B","B-","C+","C","C-","D+","D","F","FX"})
            gradeDist.put(g, 0);

        for (Mark m : marks) {
            String att1  = m.getAtt1()      != null ? String.format("%.1f", m.getAtt1())      : "-";
            String att2  = m.getAtt2()      != null ? String.format("%.1f", m.getAtt2())      : "-";
            String fin   = m.getFinalExam() != null ? String.format("%.1f", m.getFinalExam()) : "-";
            String total = m.getTotal()     != null ? String.format("%.1f", m.getTotal())     : "-";
            String grade = m.getLetterGrade();

            sb.append(String.format("%-14s %-7s %-7s %-7s %-7s %-6s%n",
                    m.getStudentId(), att1, att2, fin, total, grade));

            // count stats when outcome is known (att1+att2 set, and either not admitted or final is set)
            boolean hasOutcome = m.getAtt1() != null && m.getAtt2() != null
                    && (!m.isAdmittedToFinal() || m.getFinalExam() != null);
            if (hasOutcome) {
                gradedCount++;
                if (m.isPassed()) {
                    passed++;
                    sumTotal += m.getTotal();
                } else if (m.isFX()) {
                    fx++;
                    if (m.getTotal() != null) sumTotal += m.getTotal();
                } else {
                    failed++;
                }
                if (!grade.equals("N/A")) gradeDist.merge(grade, 1, Integer::sum);
            }
        }

        sb.append("------------------------------------------------------------\n");
        double avgTotal = gradedCount > 0 ? sumTotal / gradedCount : 0.0;
        int    passRate = gradedCount > 0 ? (int) Math.round(passed * 100.0 / gradedCount) : 0;

        sb.append(String.format(
                " Students: %d   Passed: %d   FX: %d   Failed: %d   Pass rate: %d%%%n",
                totalStudents, passed, fx, failed, passRate));
        sb.append(String.format(" Average total (graded): %.1f%n", avgTotal));
        sb.append(" Grade distribution:");
        for (Map.Entry<String, Integer> e : gradeDist.entrySet()) {
            sb.append(String.format("  %s=%d", e.getKey(), e.getValue()));
        }
        sb.append("\n");
        sb.append("============================================================\n");
        return sb.toString();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String key(String studentId, String courseCode) {
        return studentId + ":" + courseCode;
    }
}
