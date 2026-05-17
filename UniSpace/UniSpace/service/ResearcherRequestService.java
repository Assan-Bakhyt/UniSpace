package UniSpace.service;

import UniSpace.exception.ValidationException;
import UniSpace.model.research.ResearcherRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages researcher role requests submitted by Students and Teachers.
 * Reviewed and approved/rejected by a Manager.
 * Pattern: Singleton.
 */
public class ResearcherRequestService {

    private static ResearcherRequestService instance;

    public static ResearcherRequestService getInstance() {
        if (instance == null) instance = new ResearcherRequestService();
        return instance;
    }

    private ResearcherRequestService() {}

    private final List<ResearcherRequest> requests = new ArrayList<>();

    // ── Student / Teacher side ────────────────────────────────────────────────

    /**
     * Submits a PENDING researcher role request.
     *
     * @throws ValidationException if a pending request from this applicant already exists
     */
    public ResearcherRequest submitRequest(String applicantId, String applicantName,
                                           String applicantRole) throws ValidationException {
        boolean alreadyPending = requests.stream()
                .anyMatch(r -> r.getApplicantId().equals(applicantId) && r.isPending());

        if (alreadyPending)
            throw new ValidationException("request",
                    "You already have a pending researcher request.");

        ResearcherRequest req = new ResearcherRequest(applicantId, applicantName, applicantRole);
        requests.add(req);
        return req;
    }

    // ── Manager side ──────────────────────────────────────────────────────────

    /**
     * Approves the request and activates researcher role for the applicant.
     * Sends an inbox notification to the applicant.
     *
     * @throws ValidationException if not found or already processed
     */
    public ResearcherRequest approveRequest(String requestId) throws ValidationException {
        ResearcherRequest req = findById(requestId)
                .orElseThrow(() -> new ValidationException("requestId", "Request not found."));

        if (!req.isPending())
            throw new ValidationException("request",
                    "Request already processed: " + req.getStatus());

        req.approve();
        return req;
    }

    /**
     * Rejects the request with a reason.
     * Sends an inbox notification to the applicant.
     *
     * @throws ValidationException if not found or already processed
     */
    public ResearcherRequest rejectRequest(String requestId, String reason)
            throws ValidationException {
        ResearcherRequest req = findById(requestId)
                .orElseThrow(() -> new ValidationException("requestId", "Request not found."));

        if (!req.isPending())
            throw new ValidationException("request",
                    "Request already processed: " + req.getStatus());

        req.reject(reason);
        return req;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<ResearcherRequest> getPendingRequests() {
        return requests.stream()
                .filter(ResearcherRequest::isPending)
                .collect(Collectors.toList());
    }

    public List<ResearcherRequest> getRequestsByApplicant(String applicantId) {
        return requests.stream()
                .filter(r -> r.getApplicantId().equals(applicantId))
                .collect(Collectors.toList());
    }

    public boolean hasPendingRequest(String applicantId) {
        return requests.stream()
                .anyMatch(r -> r.getApplicantId().equals(applicantId) && r.isPending());
    }

    public List<ResearcherRequest> getAllRequests() {
        return Collections.unmodifiableList(requests);
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public List<ResearcherRequest> getRequestsForStore() {
        return new ArrayList<>(requests);
    }

    public void restoreRequests(List<ResearcherRequest> loaded) {
        requests.clear();
        if (loaded != null) requests.addAll(loaded);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Optional<ResearcherRequest> findById(String requestId) {
        return requests.stream()
                .filter(r -> r.getRequestId().equals(requestId))
                .findFirst();
    }
}
