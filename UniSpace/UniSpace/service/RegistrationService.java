package UniSpace.service;

import UniSpace.exception.CourseRegistrationException;
import UniSpace.model.course.RegistrationRequest;
import UniSpace.model.course.RegistrationRequest.RequestStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages student course registration requests and manager approval workflow.
 * Pattern: Singleton.
 *
 * Flow:
 *   Student submits request → PENDING
 *   Manager approves → APPROVED + courseService.registerStudentForCourse()
 *   Manager rejects  → REJECTED + optional note
 */
public class RegistrationService {

    private static RegistrationService instance;

    public static RegistrationService getInstance() {
        if (instance == null) instance = new RegistrationService();
        return instance;
    }

    private RegistrationService() {}

    private final List<RegistrationRequest> requests = new ArrayList<>();

    // ── Student side ──────────────────────────────────────────────────────────

    /**
     * Creates a PENDING registration request.
     *
     * @throws CourseRegistrationException if a pending request for this course already exists
     */
    public RegistrationRequest submitRequest(String studentId, String courseCode)
            throws CourseRegistrationException {

        boolean alreadyPending = requests.stream()
                .anyMatch(r -> r.getStudentId().equals(studentId)
                        && r.getCourseCode().equals(courseCode)
                        && r.isPending());

        if (alreadyPending)
            throw new CourseRegistrationException(
                    "A pending request for course " + courseCode + " already exists.");

        RegistrationRequest request = new RegistrationRequest(studentId, courseCode);
        requests.add(request);
        return request;
    }

    // ── Manager side ──────────────────────────────────────────────────────────

    /**
     * Approves a request and enrolls the student in the course.
     *
     * @throws CourseRegistrationException if not found, already processed,
     *                                     or courseService throws (credit/fail limits)
     */
    public RegistrationRequest approveRequest(String requestId, CourseService courseService)
            throws CourseRegistrationException {

        RegistrationRequest request = findById(requestId)
                .orElseThrow(() -> new CourseRegistrationException(
                        "Request not found: " + requestId));

        if (!request.isPending())
            throw new CourseRegistrationException(
                    "Request already processed: " + request.getStatus());

        courseService.registerStudentForCourse(request.getStudentId(), request.getCourseCode());
        request.setStatus(RequestStatus.APPROVED);
        return request;
    }

    /**
     * Rejects a request with an optional reason.
     *
     * @throws CourseRegistrationException if not found or already processed
     */
    public void rejectRequest(String requestId, String reason)
            throws CourseRegistrationException {

        RegistrationRequest request = findById(requestId)
                .orElseThrow(() -> new CourseRegistrationException(
                        "Request not found: " + requestId));

        if (!request.isPending())
            throw new CourseRegistrationException(
                    "Request already processed: " + request.getStatus());

        request.setStatus(RequestStatus.REJECTED);
        request.setManagerNote(reason);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<RegistrationRequest> getPendingRequests() {
        return requests.stream()
                .filter(RegistrationRequest::isPending)
                .collect(Collectors.toList());
    }

    public List<RegistrationRequest> getRequestsByStudent(String studentId) {
        return requests.stream()
                .filter(r -> r.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    public Optional<RegistrationRequest> getRequestById(String requestId) {
        return findById(requestId);
    }

    public List<RegistrationRequest> getAllRequests() {
        return Collections.unmodifiableList(requests);
    }

    // ── Restore from disk ──────────────────────────────────────────────────────

    public void restoreRequests(List<RegistrationRequest> loaded) {
        requests.clear();
        if (loaded != null) requests.addAll(loaded);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Optional<RegistrationRequest> findById(String requestId) {
        return requests.stream()
                .filter(r -> r.getRequestId().equals(requestId))
                .findFirst();
    }
}
