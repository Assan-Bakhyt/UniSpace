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

    public int getUnreadCount(String userId) {
        MessageObserver obs = observers.get(userId);
        return obs != null ? obs.getUnreadCount() : 0;
    }

    public void markAllRead(String userId) {
        MessageObserver obs = observers.get(userId);
        if (obs != null) obs.markAllRead();
    }

    // ── Persistence snapshots ─────────────────────────────────────────────────

    public java.util.Map<String, java.util.List<String>> getInboxMessages() {
        java.util.Map<String, java.util.List<String>> result = new java.util.HashMap<>();
        observers.forEach((id, obs) -> result.put(id, new java.util.ArrayList<>(obs.getInbox())));
        return result;
    }

    public java.util.Map<String, Integer> getReadIndices() {
        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        observers.forEach((id, obs) -> result.put(id, obs.getReadIndex()));
        return result;
    }

    public void restoreUserInbox(String userId, java.util.List<String> messages, int readIndex) {
        MessageObserver obs = observers.get(userId);
        if (obs != null) obs.restoreInbox(messages, readIndex);
    }
}
