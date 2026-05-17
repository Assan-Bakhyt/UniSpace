package UniSpace.model.user;

import UniSpace.enums.Faculty;
import UniSpace.enums.TeacherTitle;
import UniSpace.enums.UserRole;
import UniSpace.exception.ValidationException;
import UniSpace.model.course.Course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Teacher extends Employee implements Comparable<Teacher> {

    private TeacherTitle title;
    private List<Course> courses;
    private double rating;
    private int ratingCount;

    /** studentId → rating given by that student (prevents double-rating, allows update). */
    private Map<String, Double> studentRatings = new HashMap<>();

    public Teacher() {}

    public Teacher(String id, String firstName, String lastName,
                   String email, String password,
                   Faculty department, double salary, TeacherTitle title) {
        super(id, firstName, lastName, email, password,
                UserRole.TEACHER, department, salary);
        this.courses = new ArrayList<>();
        setTitle(title);
    }

    // ── Title ─────────────────────────────────────────────────────────────────

    public void setTitle(TeacherTitle title) {
        this.title = title;
        if (title == TeacherTitle.PROFESSOR) {
            activateResearcher();
        }
    }

    // ── Researcher override ───────────────────────────────────────────────────

    @Override
    public void setResearcher(boolean researcher) {
        if (title == TeacherTitle.PROFESSOR) return;
        super.setResearcher(researcher);
    }

    // ── Rating ────────────────────────────────────────────────────────────────

    /**
     * Adds or updates a student's rating for this teacher.
     * Each student may rate only once; repeated calls update the existing rating.
     */
    public void addRating(String studentId, double newRating) throws ValidationException {
        if (newRating < 1 || newRating > 5)
            throw new ValidationException("rating", "Rating must be between 1 and 5");
        if (studentRatings == null) studentRatings = new HashMap<>();
        studentRatings.put(studentId, newRating);
        // Recalculate average from all student ratings
        this.rating = studentRatings.values().stream()
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
        this.ratingCount = studentRatings.size();
    }

    /** True if this student has already submitted a rating for this teacher. */
    public boolean hasRatedBy(String studentId) {
        return studentRatings != null && studentRatings.containsKey(studentId);
    }

    // ── Courses ───────────────────────────────────────────────────────────────

    public void addCourse(Course course) {
        if (courses == null) courses = new ArrayList<>();
        if (!courses.contains(course)) courses.add(course);
    }

    public void removeCourse(Course course) {
        if (courses != null) courses.remove(course);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public TeacherTitle getTitle()       { return title; }
    public List<Course> getCourses()     { return courses != null ? Collections.unmodifiableList(courses) : Collections.emptyList(); }
    public double       getRating()      { return rating; }
    public int          getRatingCount() { return ratingCount; }

    // ── Comparable: descending rating ─────────────────────────────────────────

    @Override
    public int compareTo(Teacher other) {
        return Double.compare(other.rating, this.rating);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(
                " | Title: %s | Rating: %.1f (%d) | Researcher: %s",
                title, rating, ratingCount, isResearcher());
    }
}
