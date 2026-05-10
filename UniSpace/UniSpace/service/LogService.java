package UniSpace.service;

import UniSpace.model.log.LogEntry;
import UniSpace.patterns.Observer;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Singleton service responsible for managing system logs.
 * Implements the Observer pattern to automatically log user actions.
 * All user activities are recorded for audit and monitoring purposes.
 */
public class LogService implements Serializable, Observer {

    private static final long serialVersionUID = 1L;

    /** Singleton instance */
    private static LogService instance;

    /** List of all log entries */
    private final List<LogEntry> logs;

    /** Counter for generating unique log IDs */
    private static final AtomicLong idCounter = new AtomicLong(0);

    /**
     * Private constructor for Singleton pattern.
     * Initializes the logs list.
     */
    private LogService() {
        this.logs = new ArrayList<>();
    }

    /**
     * Gets the singleton instance of LogService.
     *
     * @return the singleton LogService instance
     */
    public static synchronized LogService getInstance() {
        if (instance == null) {
            instance = new LogService();
        }
        return instance;
    }

    /**
     * Logs a user action with the specified details.
     * This is the main method for recording user activities.
     *
     * @param userId   ID of the user who performed the action
     * @param userName name of the user who performed the action
     * @param action   description of the action performed
     * @return the created LogEntry
     */
    public LogEntry log(String userId, String userName, String action) {
        String logId = "LOG-" + idCounter.incrementAndGet();
        LogEntry entry = new LogEntry(logId, userId, userName, action);
        logs.add(entry);
        System.out.println("[LOG] " + entry);
        return entry;
    }

    /**
     * Logs a user action using only user ID (name will be resolved later or set as "Unknown").
     * Convenience method when username is not immediately available.
     *
     * @param userId ID of the user who performed the action
     * @param action description of the action performed
     * @return the created LogEntry
     */
    public LogEntry log(String userId, String action) {
        return log(userId, "Unknown", action);
    }

    /**
     * Gets all log entries in the system.
     *
     * @return unmodifiable list of all log entries
     */
    public List<LogEntry> getAllLogs() {
        return Collections.unmodifiableList(logs);
    }

    /**
     * Gets log entries for a specific user.
     *
     * @param userId ID of the user to filter logs by
     * @return list of log entries for the specified user
     */
    public List<LogEntry> getLogsByUserId(String userId) {
        List<LogEntry> userLogs = new ArrayList<>();
        for (LogEntry entry : logs) {
            if (entry.getUserId().equals(userId)) {
                userLogs.add(entry);
            }
        }
        return Collections.unmodifiableList(userLogs);
    }

    /**
     * Gets the most recent log entries up to the specified count.
     *
     * @param count maximum number of recent logs to return
     * @return list of recent log entries
     */
    public List<LogEntry> getRecentLogs(int count) {
        int startIndex = Math.max(0, logs.size() - count);
        return Collections.unmodifiableList(new ArrayList<>(logs.subList(startIndex, logs.size())));
    }

    /**
     * Clears all log entries from the system.
     * Use with caution - this operation cannot be undone.
     */
    public void clearLogs() {
        logs.clear();
        System.out.println("[LOG] All logs cleared.");
    }

    /**
     * Gets the total number of log entries in the system.
     *
     * @return the count of log entries
     */
    public int getLogCount() {
        return logs.size();
    }

    /**
     * Observer pattern implementation - called when an observable event occurs.
     * Automatically logs the event.
     *
     * @param fromName name of the entity triggering the event
     * @param message  description of the event
     */
    @Override
    public void update(String fromName, String message) {
        log("SYSTEM", fromName, message);
    }

    @Override
    public String toString() {
        return "LogService [totalLogs=" + logs.size() + "]";
    }
}