package UniSpace.model.user;

import UniSpace.enums.Faculty;
import UniSpace.enums.UserRole;
import java.io.Serializable;
import java.util.Objects;

public abstract class User implements Serializable {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private UserRole role;

    public User() {}

    public User(String id, String firstName, String lastName,
                String email, String password, UserRole role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }

    public void setId(String id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(UserRole role) { this.role = role; }

    /** Returns the faculty this user belongs to. Overridden by Employee and Student. */
    public Faculty getFaculty() { return null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | Role: %s", id, getFullName(), email, role);
    }
}
