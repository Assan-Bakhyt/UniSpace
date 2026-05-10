package UniSpace.model.course;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class RegistrationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum RequestStatus { PENDING, APPROVED, REJECTED } // Сделать Отдельный энам

    private String        requestId;
    private String        studentId;
    private String        courseCode;
    private RequestStatus status;
    private LocalDateTime requestDate;
    private String        managerNote;

    public RegistrationRequest() {}

    public RegistrationRequest(String studentId, String courseCode) {
        this.requestId   = UUID.randomUUID().toString();
        this.studentId   = studentId;
        this.courseCode  = courseCode;
        this.status      = RequestStatus.PENDING;
        this.requestDate = LocalDateTime.now();
    }

    public boolean isPending() { return status == RequestStatus.PENDING; }

    public String        getRequestId()   { return requestId; }
    public String        getStudentId()   { return studentId; }
    public String        getCourseCode()  { return courseCode; }
    public RequestStatus getStatus()      { return status; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public String        getManagerNote() { return managerNote; }

    public void setStatus(RequestStatus status)    { this.status      = status; }
    public void setManagerNote(String managerNote) { this.managerNote = managerNote; }

    @Override
    public String toString() {
        String note = managerNote != null ? " | Note: " + managerNote : "";
        return String.format("[%s] Student %-8s → %-8s  (%s)%s",
                status, studentId, courseCode,
                requestDate.toLocalDate(), note);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistrationRequest)) return false;
        return Objects.equals(requestId, ((RegistrationRequest) o).requestId);
    }

    @Override
    public int hashCode() { return Objects.hash(requestId); }
}
