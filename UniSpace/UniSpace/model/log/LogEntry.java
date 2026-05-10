package UniSpace.model.log;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a log entry event in the system.
 * Captures who performed an action, what action was performed, and when it happened.
 * Used for audit trails and tracking user activities.
 */
public class LogEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique identifier for the log entry */
    private final String id;

    /** ID of the user who performed the action */
    private final String userId;

    /** Name of the user who performed the action */
    private final String userName;

    /** Description of the action performed */
    private final String action;

    /** Timestamp when the action occurred */
    private final LocalDateTime timestamp;

    /**
     * Creates a new LogEntry with the specified details.
     *
     * @param id unique identifier for this log entry
     * @param userId ID of the user who performed the action
     * @param userName name of the user who performed the action
     * @param action description of the action performed
     */
    public LogEntry(String id, String userId, String userName, String action) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Gets the unique identifier of this log entry.
     *
     * @return the log entry ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the ID of the user who performed the action.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the name of the user who performed the action.
     *
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets the description of the action performed.
     *
     * @return the action description
     */
    public String getAction() {
        return action;
    }

    /**
     * Gets the timestamp when the action occurred.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogEntry)) return false;
        LogEntry logEntry = (LogEntry) o;
        return Objects.equals(id, logEntry.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | User: %s (%s) | Action: %s",
                timestamp, id, userName, userId, action);
    }
}