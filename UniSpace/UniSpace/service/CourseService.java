package UniSpace.service;

import UniSpace.exception.CreditLimitException;
import UniSpace.exception.CourseRegistrationException;
import UniSpace.exception.MaxFailsException;
import UniSpace.model.course.Course;
import UniSpace.model.course.Mark;

import java.util.*;

/**
 * Handles course catalogue, student registration, and instructor assignment.
 *
 * Data is kept in plain in-memory maps — no DB layer required for Part B.
 *
 * studentCourses   : studentId  → list of enrolled course codes
 * studentCredits   : studentId  → total enrolled credits
 * failCounts       : studentId:courseCode → number of times failed
 * courseMarks      : studentId:courseCode → Mark record
 */
public class CourseService {

    private static final int MAX_CREDITS = 21;
    private static final int MAX_FAILS   = 3;

    /** All courses in the system, keyed by courseCode */
    private final Map<String, Course> courseMap = new HashMap<>();

    /** studentId → set of enrolled course codes */
    private final Map<String, Set<String>> studentCourses = new HashMap<>();

    /** studentId → total credits currently registered */
    private final Map<String, Integer> studentCredits = new HashMap<>();

    /**
     * "studentId:courseCode" → fail count
     * Populated by MarkService via {@link #recordFail(String, String)}.
     */
    private final Map<String, Integer> failCounts = new HashMap<>();

    // ── Course catalogue ──────────────────────────────────────────────────────

    public void addCourse(Course course) {
        courseMap.put(course.getCourseCode(), course);
    }

    public Optional<Course> findCourse(String courseCode) {
        return Optional.ofNullable(courseMap.get(courseCode));
    }

    public Collection<Course> getAllCourses() {
        return Collections.unmodifiableCollection(courseMap.values());
    }

    // ── Instructor assignment ─────────────────────────────────────────────────

    /**
     * Assigns a teacher to a course. A course can have more than one instructor.
     */
    public void assignInstructor(String courseCode, String teacherId)
            throws CourseRegistrationException {
        Course course = courseMap.get(courseCode);
        if (course == null)
            throw new CourseRegistrationException("Course not found: " + courseCode);
        course.addInstructor(teacherId);
    }

    /**
     * Returns all courses taught by the given teacher.
     */
    public List<Course> getCoursesByInstructor(String teacherId) {
        List<Course> result = new ArrayList<>();
        for (Course c : courseMap.values()) {
            if (c.hasInstructor(teacherId)) result.add(c);
        }
        return result;
    }

    // ── Student registration ──────────────────────────────────────────────────

    /**
     * Registers a student for a course.
     *
     * @throws CreditLimitException           if adding this course exceeds 21 credits
     * @throws MaxFailsException              if the student has failed this course 3+ times
     * @throws CourseRegistrationException    if already registered or course not found
     */
    public void registerStudentForCourse(String studentId, String courseCode)
            throws CourseRegistrationException {

        Course course = courseMap.get(courseCode);
        if (course == null)
            throw new CourseRegistrationException("Course not found: " + courseCode);

        Set<String> enrolled = studentCourses.computeIfAbsent(studentId, k -> new HashSet<>());
        if (enrolled.contains(courseCode))
            throw new CourseRegistrationException(
                    "Student " + studentId + " is already registered for " + courseCode);

        // Check fail limit
        int fails = failCounts.getOrDefault(failKey(studentId, courseCode), 0);
        if (fails >= MAX_FAILS)
            throw new MaxFailsException(courseCode);

        // Check credit limit
        int current = studentCredits.getOrDefault(studentId, 0);
        if (current + course.getCredits() > MAX_CREDITS)
            throw new CreditLimitException(current, course.getCredits());

        // All checks passed — enroll
        enrolled.add(courseCode);
        studentCredits.put(studentId, current + course.getCredits());
    }

    /**
     * Drops a student from a course (e.g., withdrawal).
     */
    public void dropCourse(String studentId, String courseCode)
            throws CourseRegistrationException {

        Set<String> enrolled = studentCourses.getOrDefault(studentId, Collections.emptySet());
        if (!enrolled.contains(courseCode))
            throw new CourseRegistrationException(
                    "Student " + studentId + " is not registered for " + courseCode);

        enrolled.remove(courseCode);
        Course course = courseMap.get(courseCode);
        if (course != null) {
            int current = studentCredits.getOrDefault(studentId, 0);
            studentCredits.put(studentId, Math.max(0, current - course.getCredits()));
        }
    }

    /**
     * Returns the list of courses a student is enrolled in.
     */
    public List<Course> getStudentCourses(String studentId) {
        Set<String> codes = studentCourses.getOrDefault(studentId, Collections.emptySet());
        List<Course> result = new ArrayList<>();
        for (String code : codes) {
            Course c = courseMap.get(code);
            if (c != null) result.add(c);
        }
        return result;
    }

    /**
     * Returns total enrolled credits for a student.
     */
    public int getStudentCredits(String studentId) {
        return studentCredits.getOrDefault(studentId, 0);
    }

    // ── Fail tracking (called by MarkService) ─────────────────────────────────

    /**
     * Records one failed attempt for a student in a course.
     * Called automatically when a final mark is saved with a failing grade.
     */
    public void recordFail(String studentId, String courseCode) {
        String key = failKey(studentId, courseCode);
        failCounts.put(key, failCounts.getOrDefault(key, 0) + 1);
    }

    public int getFailCount(String studentId, String courseCode) {
        return failCounts.getOrDefault(failKey(studentId, courseCode), 0);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String failKey(String studentId, String courseCode) {
        return studentId + ":" + courseCode;
    }
}
