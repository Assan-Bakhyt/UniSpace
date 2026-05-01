package UniSpace.storage;

import UniSpace.model.user.User;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Serializable snapshot of all persistent data.
 * FileStorage reads/writes this object; DataRepository owns the live copy.
 */
public class DataStore implements Serializable {

    private final Map<String, User> users;

    public DataStore(Map<String, User> users) {
        this.users = new HashMap<>(users);
    }

    public Map<String, User> getUsers() { return users; }
}
