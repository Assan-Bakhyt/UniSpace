package UniSpace.model.user;

import UniSpace.enums.UserRole;
import UniSpace.exception.CreditLimitException;
import UniSpace.exception.HIndexException;
import UniSpace.exception.ValidationException;
import UniSpace.model.course.Course;
import UniSpace.model.course.Mark;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Student extends User implements Comparable<Student> {

    public static final int MAX_CREDITS = 21;
    public static final int MAX_FAILS = 3;

    private int year;
    private String major;
    private double gpa;
    private int totalCredits;
    private int failCount;
    private List<Course> registeredCourses;
    private List<Mark> marks;
    private boolean isResearcher;

//     supervisor — тип Researcher, его добавит коллега из research-части
//     Нужно согласовать интерфейс:
//     private Researcher supervisor;

    public Student() {}

    public Student(String id, String firstName, String lastName,
                   String email, String password,
                   int year, String major) {
        super(id, firstName, lastName, email, password, UserRole.STUDENT);
        this.year = year;
        this.major = major;
        this.gpa = 0.0;
        this.totalCredits = 0;
        this.failCount = 0;
        this.registeredCourses = new ArrayList<>();
        this.marks = new ArrayList<>();
        this.isResearcher = false;
    }

    public void registerCourse(Course course) throws CreditLimitException {
        int newTotal = totalCredits + course.getCredits();
        if (newTotal > MAX_CREDITS) {
            throw new CreditLimitException(newTotal, MAX_CREDITS);
        }
        if (!registeredCourses.contains(course)) {
            registeredCourses.add(course);
            totalCredits += course.getCredits();
        }
    }

    public void dropCourse(Course course) {
        if (registeredCourses.remove(course)) {
            totalCredits -= course.getCredits();
        }
    }

    public void recordFail() throws ValidationException {
        this.failCount++;
        if (failCount > MAX_FAILS) {
            throw new ValidationException("failCount",
                    "Student exceeded maximum allowed fails (" + MAX_FAILS + ")");
        }
    }

    public void addMark(Mark mark) {
        marks.add(mark);
        recalculateGpa();
    }

    private void recalculateGpa() {
        if (marks.isEmpty()) { gpa = 0.0; return; }
        gpa = marks.stream()
                .mapToDouble(Mark::getTotalScore)
                .average()
                .orElse(0.0);
    }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public double getGpa() { return gpa; }
    public int getTotalCredits() { return totalCredits; }
    public int getFailCount() { return failCount; }
    public boolean isResearcher() { return isResearcher; }
    public void setResearcher(boolean researcher) { this.isResearcher = researcher; }
    public List<Course> getRegisteredCourses() { return Collections.unmodifiableList(registeredCourses); }
    public List<Mark> getMarks() { return Collections.unmodifiableList(marks); }

    @Override
    public int compareTo(Student other) {
        return Double.compare(other.gpa, this.gpa); // по убыванию GPA
    }

    @Override
    public String toString() {
        return super.toString() + String.format(
                " | Year: %d | Major: %s | GPA: %.2f | Credits: %d/%d",
                year, major, gpa, totalCredits, MAX_CREDITS);
    }
}
