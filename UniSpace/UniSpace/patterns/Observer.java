package UniSpace.patterns;

/**
 * Observer interface for the notification / messaging system.
 * Pattern: Observer (GoF).
 */
public interface Observer {
    void update(String fromName, String message);
}
