package UniSpace.model.user;

import UniSpace.enums.Faculty;
import UniSpace.enums.TeacherTitle;
import UniSpace.enums.UserRole;
import UniSpace.exception.ValidationException;
import UniSpace.model.course.Course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Teacher extends Employee implements Comparable<Teacher> {

    private TeacherTitle title;
    private List<Course> courses;
    private double rating;
    private int    ratingCount;

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
            activateResearcher(); // professors are always researchers (inherited from Employee)
        }
    }

    // ── Researcher override ───────────────────────────────────────────────────

    /** Professors can never lose researcher status. */
    @Override
    public void setResearcher(boolean researcher) {
        if (title == TeacherTitle.PROFESSOR) return;
        super.setResearcher(researcher);
    }

    // ── Rating ────────────────────────────────────────────────────────────────

    public void addRating(double newRating) throws ValidationException {
        if (newRating < 1 || newRating > 5)
            throw new ValidationException("rating", "Rating must be between 1 and 5");
        this.rating = (this.rating * ratingCount + newRating) / (ratingCount + 1);
        this.ratingCount++;
    }

    // ── Courses ───────────────────────────────────────────────────────────────

    public void addCourse(Course course) {
        if (!courses.contains(course)) courses.add(course);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public TeacherTitle getTitle()       { return title; }
    public List<Course> getCourses()     { return Collections.unmodifiableList(courses); }
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
                " | Title: %s | Rating: %.1f | Researcher: %s",
                title, rating, isResearcher());
    }
}
