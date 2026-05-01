package UniSpace.service;

import UniSpace.exception.AuthenticationException;
import UniSpace.exception.ValidationException;
import UniSpace.model.user.User;
import UniSpace.storage.DataRepository;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    // ===== SINGLETON =====
    private static AuthService instance;

    private AuthService() {
        this.users = DataRepository.getInstance().getUsers();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    // ===== ПОЛЯ =====
    private Map<String, User> users; // email → User
    private User currentUser;

    // ===== МЕТОДЫ =====

    public User login(String email, String password) throws AuthenticationException {
        User user = users.get(email);

        if (user == null) {
            throw new AuthenticationException("User with email '" + email + "' not found");
        }
        if (!user.getPassword().equals(password)) {
            throw new AuthenticationException("Incorrect password");
        }

        currentUser = user;
        System.out.println("Login successful!");
        return user;
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("Goodbye, " + currentUser.getFullName() + "!");
            currentUser = null;
        }
    }

    public void registerUser(User user) throws ValidationException {
        if (users.containsKey(user.getEmail())) {
            throw new ValidationException("email", "Email already registered");
        }
        users.put(user.getEmail(), user);
        DataRepository.getInstance().save();
    }

    public void removeUser(String email) throws ValidationException {
        if (!users.containsKey(email)) {
            throw new ValidationException("email", "User not found");
        }
        users.remove(email);
        DataRepository.getInstance().save();
    }

    public void updateUser(User user) throws ValidationException {
        if (!users.containsKey(user.getEmail())) {
            throw new ValidationException("email", "User not found");
        }
        users.put(user.getEmail(), user);
        DataRepository.getInstance().save();
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Map<String, User> getAllUsers() {
        return users;
    }
}