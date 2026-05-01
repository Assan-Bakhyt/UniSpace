package UniSpace.service;

import UniSpace.model.course.Course;
import UniSpace.model.course.Mark;

import java.util.*;

/**
 * Manages grade entry and transcript generation.
 */
public class MarkService {

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
     */
    public void setAtt2(String studentId, String courseCode, double score) {
        getOrCreateMark(studentId, courseCode).setAtt2(score);
    }

    /**
     * Sets final exam score (0–40) for a student in a course.
     * After saving, automatically records a fail if the student did not pass.
     */
    public void setFinalExam(String studentId, String courseCode, double score) {
        Mark mark = getOrCreateMark(studentId, courseCode);
        mark.setFinalExam(score);

        // Auto-record fail when all components are present and student didn't pass
        if (!mark.isPassed()) {
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
                    ? String.format("%.1f", mark.getGpaPoints()) : "-";

            sb.append(String.format("%-12s %-8d %-6s %-6s %-7s %-7s %-6s %-4s%n",
                    course.getCourseCode(), course.getCredits(),
                    att1, att2, fin, total, grade, gpa));

            if (mark != null && mark.getTotal() != null) {
                totalGpaPoints += mark.getGpaPoints() * course.getCredits();
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
        int failed        = 0;
        double sumTotal   = 0.0;
        int gradedCount   = 0;
        Map<String, Integer> gradeDist = new LinkedHashMap<>();
        for (String g : new String[]{"A","B","C","D","F"}) gradeDist.put(g, 0);

        for (Mark m : marks) {
            String att1  = m.getAtt1()      != null ? String.format("%.1f", m.getAtt1())      : "-";
            String att2  = m.getAtt2()      != null ? String.format("%.1f", m.getAtt2())      : "-";
            String fin   = m.getFinalExam() != null ? String.format("%.1f", m.getFinalExam()) : "-";
            String total = m.getTotal()     != null ? String.format("%.1f", m.getTotal())     : "-";
            String grade = m.getLetterGrade();

            sb.append(String.format("%-14s %-7s %-7s %-7s %-7s %-6s%n",
                    m.getStudentId(), att1, att2, fin, total, grade));

            // accumulate stats only when fully graded
            if (m.getTotal() != null) {
                sumTotal += m.getTotal();
                gradedCount++;
                if (m.isPassed()) passed++; else failed++;
                gradeDist.merge(grade, 1, Integer::sum);
            }
        }

        sb.append("------------------------------------------------------------\n");
        double avgTotal  = gradedCount > 0 ? sumTotal / gradedCount : 0.0;
        int    passRate  = gradedCount > 0 ? (int) Math.round(passed * 100.0 / gradedCount) : 0;

        sb.append(String.format(" Students: %d   Passed: %d   Failed: %d   Pass rate: %d%%%n",
                totalStudents, passed, failed, passRate));
        sb.append(String.format(" Average total: %.1f%n", avgTotal));
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
