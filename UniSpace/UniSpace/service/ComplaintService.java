package UniSpace.service;

import UniSpace.enums.ComplaintStatus;
import UniSpace.model.complaint.Complaint;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Singleton service responsible for managing user complaints.
 * Stores, retrieves, and manages the lifecycle of complaints in the system.
 * Allows administrators to review and update complaint statuses.
 * Implements basic persistence logic via Serializable interface.
 */
public class ComplaintService implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Singleton instance */
    private static ComplaintService instance;

    /** Map of complaints by their ID */
    private final Map<String, Complaint> complaints;

    /** Counter for generating unique complaint IDs */
    private static final AtomicLong idCounter = new AtomicLong(0);

    /**
     * Private constructor for Singleton pattern.
     * Initializes the complaints map.
     */
    private ComplaintService() {
        this.complaints = new HashMap<>();
    }

    /**
     * Gets the singleton instance of ComplaintService.
     * Thread-safe implementation using double-checked locking logic (simplified here with synchronized).
     *
     * @return the singleton ComplaintService instance
     */
    public static synchronized ComplaintService getInstance() {
        if (instance == null) {
            instance = new ComplaintService();
        }
        return instance;
    }

    /**
     * Submits a new complaint to the system.
     * The complaint is stored and assigned a unique ID.
     *
     * @param senderId      ID of the user submitting the complaint
     * @param senderName    name of the user submitting the complaint
     * @param recipientId   ID of the user the complaint is about
     * @param recipientName name of the user the complaint is about
     * @param text          the complaint text/content
     * @return the created Complaint object with assigned ID
     */
    public Complaint submit(String senderId, String senderName,
                            String recipientId, String recipientName, String text) {
        String complaintId = "COMP-" + idCounter.incrementAndGet();
        Complaint complaint = new Complaint(complaintId, senderId, senderName,
                recipientId, recipientName, text);
        complaints.put(complaintId, complaint);
        System.out.println("[COMPLAINT] Submitted: " + complaint);
        // Integration point: Could log this action via LogService here if needed
        return complaint;
    }

    /**
     * Submits an existing complaint object to the system.
     *
     * @param complaint the complaint to submit
     * @return the submitted complaint
     */
    public Complaint submit(Complaint complaint) {
        complaints.put(complaint.getId(), complaint);
        System.out.println("[COMPLAINT] Submitted: " + complaint);
        return complaint;
    }

    /**
     * Gets a complaint by its unique ID.
     *
     * @param complaintId the ID of the complaint to retrieve
     * @return the Complaint object, or null if not found
     */
    public Complaint getComplaintById(String complaintId) {
        return complaints.get(complaintId);
    }

    /**
     * Gets all complaints in the system.
     * Returns an unmodifiable collection to prevent external modification.
     *
     * @return unmodifiable collection of all complaints
     */
    public Collection<Complaint> getAllComplaints() {
        return Collections.unmodifiableCollection(complaints.values());
    }

    /**
     * Gets all complaints submitted by a specific user.
     *
     * @param userId ID of the user who submitted the complaints
     * @return list of complaints submitted by the user
     */
    public List<Complaint> getComplaintsBySenderId(String userId) {
        List<Complaint> userComplaints = new ArrayList<>();
        for (Complaint complaint : complaints.values()) {
            if (complaint.getSenderId().equals(userId)) {
                userComplaints.add(complaint);
            }
        }
        return Collections.unmodifiableList(userComplaints);
    }

    /**
     * Gets all complaints about a specific user (recipient).
     *
     * @param userId ID of the user who is the recipient of complaints
     * @return list of complaints about the user
     */
    public List<Complaint> getComplaintsByRecipientId(String userId) {
        List<Complaint> recipientComplaints = new ArrayList<>();
        for (Complaint complaint : complaints.values()) {
            if (complaint.getRecipientId().equals(userId)) {
                recipientComplaints.add(complaint);
            }
        }
        return Collections.unmodifiableList(recipientComplaints);
    }

    /**
     * Gets all complaints with a specific status.
     *
     * @param status the status to filter by
     * @return list of complaints with the specified status
     */
    public List<Complaint> getComplaintsByStatus(ComplaintStatus status) {
        List<Complaint> statusComplaints = new ArrayList<>();
        for (Complaint complaint : complaints.values()) {
            if (complaint.getStatus() == status) {
                statusComplaints.add(complaint);
            }
        }
        return Collections.unmodifiableList(statusComplaints);
    }

    /**
     * Gets all pending complaints awaiting review.
     *
     * @return list of pending complaints
     */
    public List<Complaint> getPendingComplaints() {
        return getComplaintsByStatus(ComplaintStatus.PENDING);
    }

    /**
     * Updates the status of an existing complaint.
     *
     * @param complaintId ID of the complaint to update
     * @param status      the new status to set
     * @return true if the complaint was found and updated, false otherwise
     */
    public boolean updateComplaintStatus(String complaintId, ComplaintStatus status) {
        Complaint complaint = complaints.get(complaintId);
        if (complaint != null) {
            complaint.setStatus(status);
            System.out.println("[COMPLAINT] Updated status of " + complaintId + " to " + status);
            // Integration point: Log action
            return true;
        }
        System.out.println("[COMPLAINT] Complaint not found: " + complaintId);
        return false;
    }

    /**
     * Resolves a complaint (sets status to RESOLVED).
     *
     * @param complaintId ID of the complaint to resolve
     * @return true if the complaint was found and resolved, false otherwise
     */
    public boolean resolveComplaint(String complaintId) {
        return updateComplaintStatus(complaintId, ComplaintStatus.RESOLVED);
    }

    /**
     * Rejects a complaint (sets status to REJECTED).
     *
     * @param complaintId ID of the complaint to reject
     * @return true if the complaint was found and rejected, false otherwise
     */
    public boolean rejectComplaint(String complaintId) {
        return updateComplaintStatus(complaintId, ComplaintStatus.REJECTED);
    }

    /**
     * Gets the total number of complaints in the system.
     *
     * @return the count of complaints
     */
    public int getComplaintCount() {
        return complaints.size();
    }

    /**
     * Gets the number of pending complaints.
     *
     * @return the count of pending complaints
     */
    public int getPendingComplaintCount() {
        return getPendingComplaints().size();
    }

    @Override
    public String toString() {
        return "ComplaintService [totalComplaints=" + complaints.size() +
                ", pending=" + getPendingComplaintCount() + "]";
    }
}