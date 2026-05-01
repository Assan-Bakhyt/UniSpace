package UniSpace.model.user;

import UniSpace.enums.TeacherTitle;
import UniSpace.enums.UserRole;
import UniSpace.exception.ValidationException;
import UniSpace.model.course.Course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import UniSpace.model.research.ResearchPaper;
import UniSpace.model.research.Researcher;

public class Teacher extends Employee implements Comparable<Teacher>, Researcher {

    private TeacherTitle title;
    private List<Course> courses;
    private double rating;
    private int ratingCount;
    private boolean isResearcher;

    private int hIndex;
    private List<ResearchPaper> researchPapers = new ArrayList<>();

    public Teacher() {}

    public Teacher(String id, String firstName, String lastName,
                   String email, String password,
                   String department, double salary, TeacherTitle title) {
        super(id, firstName, lastName, email, password,
                UserRole.TEACHER, department, salary);
        this.courses = new ArrayList<>();
        this.rating = 0.0;
        this.ratingCount = 0;
        setTitle(title); // через сеттер — чтобы сработала логика с PROFESSOR
    }

    public void setTitle(TeacherTitle title) {
        this.title = title;
        if (title == TeacherTitle.PROFESSOR) {
            this.isResearcher = true; // профессор ВСЕГДА исследователь
        }
    }

    public void addRating(double newRating) throws ValidationException {
        if (newRating < 1 || newRating > 5) {
            throw new ValidationException("rating", "Rating must be between 1 and 5");
        }
        this.rating = (this.rating * ratingCount + newRating) / (ratingCount + 1);
        this.ratingCount++;
    }

    public void addCourse(Course course) {
        if (!courses.contains(course)) {
            courses.add(course);
        }
    }

    public void removeCourse(Course course) {
        courses.remove(course);
    }

    public TeacherTitle getTitle() { return title; }
    public List<Course> getCourses() { return Collections.unmodifiableList(courses); }
    public double getRating() { return rating; }
    public int getRatingCount() { return ratingCount; }
    public boolean isResearcher() { return isResearcher; }

    public void setResearcher(boolean researcher) {
        // нельзя снять статус у профессора
        if (title != TeacherTitle.PROFESSOR) {
            this.isResearcher = researcher;
        }
    }

    @Override
    public int getHIndex() {
        return hIndex;
    }

    public void setHIndex(int hIndex) {
        this.hIndex = hIndex;
    }

    @Override
    public String getName() {
        return getFullName();
    }

    @Override
    public String getSchool() {
        return getDepartment(); // у преподавателя school = department
    }

    @Override
    public List<ResearchPaper> getResearchPapers() {
        return researchPapers;
    }

    public void addResearchPaper(ResearchPaper paper) {
        researchPapers.add(paper);
    }


    @Override
    public int compareTo(Teacher other) {
        return Double.compare(other.rating, this.rating); // по убыванию рейтинга
    }

    @Override
    public String toString() {
        return super.toString() + String.format(
                " | Title: %s | Rating: %.1f | Researcher: %s",
                title, rating, isResearcher);
    }
}
