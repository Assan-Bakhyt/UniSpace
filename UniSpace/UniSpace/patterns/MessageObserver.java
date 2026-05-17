package UniSpace.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete Observer — holds one user's incoming message inbox.
 * Pattern: Observer (GoF). Registered in MessageService (the Subject).
 */
public class MessageObserver implements Observer {

    private final String ownerName;
    private final List<String> inbox = new ArrayList<>();
    private int readIndex = 0; // messages before this index have been "read"

    public MessageObserver(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public void update(String fromName, String message) {
        inbox.add(String.format("[%s → %s]: %s", fromName, ownerName, message));
    }

    public List<String> getInbox()     { return Collections.unmodifiableList(inbox); }
    public boolean hasMessages()       { return !inbox.isEmpty(); }
    public String  getOwnerName()      { return ownerName; }

    public int  getUnreadCount() { return inbox.size() - readIndex; }
    public int  getReadIndex()   { return readIndex; }
    public void markAllRead()    { readIndex = inbox.size(); }

    public void restoreInbox(List<String> messages, int savedReadIndex) {
        inbox.clear();
        if (messages != null) inbox.addAll(messages);
        this.readIndex = Math.min(savedReadIndex, inbox.size());
    }
}
