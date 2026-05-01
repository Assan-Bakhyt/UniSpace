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

    public MessageObserver(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public void update(String fromName, String message) {
        inbox.add(String.format("[%s → %s]: %s", fromName, ownerName, message));
    }

    public List<String> getInbox() { return Collections.unmodifiableList(inbox); }
    public boolean hasMessages()   { return !inbox.isEmpty(); }
    public String  getOwnerName()  { return ownerName; }
}
