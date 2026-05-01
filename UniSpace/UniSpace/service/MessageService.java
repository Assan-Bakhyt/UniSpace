package UniSpace.service;

import UniSpace.patterns.MessageObserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Subject (Observable) in the Observer pattern.
 * Manages subscriptions and routes messages between users.
 * Pattern: Singleton + Observer.
 */
public class MessageService {

    private static MessageService instance;

    public static MessageService getInstance() {
        if (instance == null) instance = new MessageService();
        return instance;
    }

    private MessageService() {}

    /** userId → observer (inbox) */
    private final Map<String, MessageObserver> observers = new HashMap<>();

    // ── Subject interface ─────────────────────────────────────────────────────

    public void register(String userId, String displayName) {
        observers.putIfAbsent(userId, new MessageObserver(displayName));
    }

    public void unregister(String userId) {
        observers.remove(userId);
    }

    /**
     * Sends a message from one user to another.
     *
     * @return true if delivered, false if the recipient is not registered
     */
    public boolean send(String fromUserId, String fromName, String toUserId, String message) {
        MessageObserver recipient = observers.get(toUserId);
        if (recipient == null) return false;
        recipient.update(fromName, message);
        return true;
    }

    public List<String> getInbox(String userId) {
        MessageObserver obs = observers.get(userId);
        return obs != null ? obs.getInbox() : Collections.emptyList();
    }

    public boolean hasMessages(String userId) {
        MessageObserver obs = observers.get(userId);
        return obs != null && obs.hasMessages();
    }
}
