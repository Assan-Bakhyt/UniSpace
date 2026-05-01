package UniSpace.model.course;

import UniSpace.enums.LessonType;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Lesson implements Serializable {
    private String lessonId;
    private String title;
    private LessonType type;
    private LocalDateTime dateTime;
    private String location;
    private Course course;

    public Lesson() {}

    public Lesson(String lessonId, String title, LessonType type, LocalDateTime dateTime, String location) {
        this.lessonId  = lessonId;
        this.title     = title;
        this.type      = type;
        this.dateTime  = dateTime;
        this.location  = location;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getLessonId()               { return lessonId; }
    public void   setLessonId(String id)      { this.lessonId = id; }

    public String getTitle()                  { return title; }
    public void   setTitle(String title)      { this.title = title; }

    public LessonType getType()               { return type; }
    public void       setType(LessonType t)   { this.type = t; }

    public LocalDateTime getDateTime()                    { return dateTime; }
    public void          setDateTime(LocalDateTime dt)    { this.dateTime = dt; }

    public String getLocation()               { return location; }
    public void   setLocation(String loc)     { this.location = loc; }

    public Course getCourse()                 { return course; }
    public void   setCourse(Course c)         { this.course = c; }

    @Override
    public String toString() {
        return String.format("Lesson{id='%s', title='%s', type=%s, dateTime=%s}",
                lessonId, title, type, dateTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lesson)) return false;
        return Objects.equals(lessonId, ((Lesson) o).lessonId);
    }

    @Override
    public int hashCode() { return Objects.hash(lessonId); }
}
