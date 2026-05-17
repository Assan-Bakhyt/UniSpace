package UniSpace.model.research;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request submitted by a Student or Teacher to be granted the Researcher role.
 * Reviewed and approved/rejected by a Manager.
 */
public class ResearcherRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status { PENDING, APPROVED, REJECTED }

    private final String        requestId;
    private final String        applicantId;
    private final String        applicantName;
    private final String        applicantRole;   // "STUDENT" or "TEACHER"
    private final LocalDateTime submittedAt;
    private Status              status;
    private String              managerNote;

    public ResearcherRequest(String applicantId, String applicantName, String applicantRole) {
        this.requestId     = UUID.randomUUID().toString();
        this.applicantId   = applicantId;
        this.applicantName = applicantName;
        this.applicantRole = applicantRole;
        this.submittedAt   = LocalDateTime.now();
        this.status        = Status.PENDING;
    }

    public boolean isPending()  { return status == Status.PENDING; }
    public boolean isApproved() { return status == Status.APPROVED; }

    public String        getRequestId()    { return requestId; }
    public String        getApplicantId()  { return applicantId; }
    public String        getApplicantName(){ return applicantName; }
    public String        getApplicantRole(){ return applicantRole; }
    public LocalDateTime getSubmittedAt()  { return submittedAt; }
    public Status        getStatus()       { return status; }
    public String        getManagerNote()  { return managerNote; }

    public void approve()                  { this.status = Status.APPROVED; }
    public void reject(String note)        { this.status = Status.REJECTED; this.managerNote = note; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) — %s | %s",
                submittedAt.toLocalDate(), applicantName, applicantRole,
                status, requestId.substring(0, 8));
    }
}
