package UniSpace.model.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a university course.
 * A course can have multiple instructors (teachers).
 */
public class Course {

<<<<<<< HEAD
    private String       courseCode;
    private String       name;
    private int          credits;
    private String       description;

    /** All lessons that belong to this course */
    private List<Lesson> lessons      = new ArrayList<>();

    /** Teacher IDs assigned to this course (>1 instructor allowed) */
    private List<String> instructorIds = new ArrayList<>();

    public Course() {}

    public Course(String courseCode, String name, int credits) {
        this.courseCode = courseCode;
        this.name       = name;
        this.credits    = credits;
    }

    // ── Instructor management ─────────────────────────────────────────────────

    public void addInstructor(String teacherId) {
        if (teacherId != null && !instructorIds.contains(teacherId))
            instructorIds.add(teacherId);
    }

    public void removeInstructor(String teacherId) {
        instructorIds.remove(teacherId);
    }

    public boolean hasInstructor(String teacherId) {
        return instructorIds.contains(teacherId);
    }

    public List<String> getInstructorIds() {
        return Collections.unmodifiableList(instructorIds);
    }

    // ── Lesson management ─────────────────────────────────────────────────────

    public void addLesson(Lesson lesson) {
        if (lesson != null) {
            lesson.setCourse(this);
            lessons.add(lesson);
        }
    }

    public List<Lesson> getLessons() {
        return Collections.unmodifiableList(lessons);
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getCourseCode()               { return courseCode; }
    public void   setCourseCode(String code)    { this.courseCode = code; }

    public String getName()                     { return name; }
    public void   setName(String name)          { this.name = name; }

    public int    getCredits()                  { return credits; }
    public void   setCredits(int credits)       { this.credits = credits; }

    public String getDescription()              { return description; }
    public void   setDescription(String desc)   { this.description = desc; }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("Course{code='%s', name='%s', credits=%d, instructors=%s}",
                courseCode, name, credits, instructorIds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        return Objects.equals(courseCode, ((Course) o).courseCode);
    }

    @Override
    public int hashCode() { return Objects.hash(courseCode); }
=======
    public int getCredits(){
        return 0; // Реализуйте!!!
    }
>>>>>>> origin/main
}
