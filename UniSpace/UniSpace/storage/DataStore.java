package UniSpace.storage;

import UniSpace.model.complaint.Complaint;
import UniSpace.model.course.Course;
import UniSpace.model.course.Mark;
import UniSpace.model.course.RegistrationRequest;
import UniSpace.model.log.LogEntry;
import UniSpace.model.news.News;
import UniSpace.model.user.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Serializable snapshot of all persistent data.
 * FileStorage reads/writes this object; DataRepository owns the live copy.
 * Pattern: Memento (stores full system state for save/restore).
 */
public class DataStore implements Serializable {

    private static final long serialVersionUID = 3L;

    // ── User data ─────────────────────────────────────────────────────────────
    private final Map<String, User>          users;

    // ── Course data ───────────────────────────────────────────────────────────
    private final Map<String, Course>        courses;
    private final Map<String, Set<String>>   studentCourses;
    private final Map<String, Integer>       studentCredits;
    private final Map<String, Integer>       failCounts;

    // ── Grade data ────────────────────────────────────────────────────────────
    private final Map<String, Mark>          marks;

    // ── Registration requests ─────────────────────────────────────────────────
    private final List<RegistrationRequest>  registrationRequests;

    // ── News ──────────────────────────────────────────────────────────────────
    private final List<News>                 newsList;

    // ── Complaints ────────────────────────────────────────────────────────────
    private final List<Complaint>            complaints;

    // ── Logs ──────────────────────────────────────────────────────────────────
    private final List<LogEntry>             logs;

    public DataStore(Map<String, User>         users,
                     Map<String, Course>        courses,
                     Map<String, Mark>          marks,
                     Map<String, Set<String>>   studentCourses,
                     Map<String, Integer>       studentCredits,
                     Map<String, Integer>       failCounts,
                     List<News>                 newsList,
                     List<RegistrationRequest>  registrationRequests,
                     List<Complaint>            complaints,
                     List<LogEntry>             logs) {

        this.users                = new HashMap<>(users);
        this.courses              = new HashMap<>(courses);
        this.marks                = new HashMap<>(marks);
        this.studentCredits       = new HashMap<>(studentCredits);
        this.failCounts           = new HashMap<>(failCounts);
        this.newsList             = new ArrayList<>(newsList);
        this.registrationRequests = new ArrayList<>(registrationRequests);
        this.complaints           = new ArrayList<>(complaints);
        this.logs                 = new ArrayList<>(logs);

        // Deep-copy the inner sets so mutations don't affect saved state
        this.studentCourses = new HashMap<>();
        for (Map.Entry<String, Set<String>> e : studentCourses.entrySet()) {
            this.studentCourses.put(e.getKey(), new HashSet<>(e.getValue()));
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Map<String, User>         getUsers()                { return users; }
    public Map<String, Course>       getCourses()              { return courses; }
    public Map<String, Mark>         getMarks()                { return marks; }
    public Map<String, Set<String>>  getStudentCourses()       { return studentCourses; }
    public Map<String, Integer>      getStudentCredits()       { return studentCredits; }
    public Map<String, Integer>      getFailCounts()           { return failCounts; }
    public List<News>                getNewsList()             { return newsList; }
    public List<RegistrationRequest> getRegistrationRequests() { return registrationRequests; }
    public List<Complaint>           getComplaints()           { return complaints; }
    public List<LogEntry>            getLogs()                 { return logs; }
}
