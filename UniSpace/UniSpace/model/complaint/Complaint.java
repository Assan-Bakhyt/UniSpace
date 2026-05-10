package UniSpace.model.complaint;

import UniSpace.enums.ComplaintStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a complaint submitted by one user about another user or issue.
 * Contains information about the sender, recipient, complaint text, and current status.
 */
public class Complaint implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique identifier for the complaint */
    private final String id;

    /** ID of the user who submitted the complaint */
    private final String senderId;

    /** Name of the user who submitted the complaint */
    private final String senderName;

    /** ID of the user the complaint is about (recipient) */
    private final String recipientId;

    /** Name of the user the complaint is about (recipient) */
    private final String recipientName;

    /** Text content of the complaint */
    private final String text;

    /** Current status of the complaint */
    private ComplaintStatus status;

    /** Timestamp when the complaint was created */
    private final LocalDateTime createdAt;

    /**
     * Creates a new Complaint with the specified details.
     * Status is automatically set to PENDING.
     *
     * @param id unique identifier for this complaint
     * @param senderId ID of the user who submitted the complaint
     * @param senderName name of the user who submitted the complaint
     * @param recipientId ID of the user the complaint is about
     * @param recipientName name of the user the complaint is about
     * @param text the complaint text/content
     * @throws IllegalArgumentException if text is null or empty
     */
    public Complaint(String id, String senderId, String senderName,
                     String recipientId, String recipientName, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Complaint text cannot be empty.");
        }
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.text = text;
        this.status = ComplaintStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Gets the unique identifier of this complaint.
     *
     * @return the complaint ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the ID of the user who submitted the complaint.
     *
     * @return the sender ID
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Gets the name of the user who submitted the complaint.
     *
     * @return the sender name
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Gets the ID of the user the complaint is about.
     *
     * @return the recipient ID
     */
    public String getRecipientId() {
        return recipientId;
    }

    /**
     * Gets the name of the user the complaint is about.
     *
     * @return the recipient name
     */
    public String getRecipientName() {
        return recipientName;
    }

    /**
     * Gets the text content of the complaint.
     *
     * @return the complaint text
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the current status of the complaint.
     *
     * @return the complaint status
     */
    public ComplaintStatus getStatus() {
        return status;
    }

    /**
     * Updates the status of this complaint.
     *
     * @param status the new status to set
     * @throws IllegalArgumentException if status is null
     */
    public void setStatus(ComplaintStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        this.status = status;
    }

    /**
     * Gets the timestamp when the complaint was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Complaint)) return false;
        Complaint complaint = (Complaint) o;
        return Objects.equals(id, complaint.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] Complaint #%s | From: %s (%s) To: %s (%s) | Status: %s%nText: %s",
                createdAt, id, senderName, senderId, recipientName, recipientId, status, text);
    }
}