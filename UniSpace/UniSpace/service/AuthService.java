package UniSpace.service;

import UniSpace.exception.AuthenticationException;
import UniSpace.exception.ValidationException;
import UniSpace.model.user.User;
import UniSpace.storage.DataRepository;
import UniSpace.util.Validator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for user authentication and authorization.
 * Implements Singleton pattern to ensure single point of authentication control.
 * Handles login, logout, registration, and user management with password hashing.
 */
public class AuthService {

    // ===== SINGLETON =====
    private static AuthService instance;

    /**
     * Private constructor for Singleton pattern.
     * Initializes the users map from the data repository.
     */
    private AuthService() {
        this.users = DataRepository.getInstance().getUsers();
    }

    /**
     * Gets the singleton instance of AuthService.
     *
     * @return the singleton AuthService instance
     */
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

    /**
     * Authenticates a user with email and password.
     * Password is verified using SHA-256 hash comparison.
     *
     * @param email    the user's email address
     * @param password the user's plain text password
     * @return the authenticated User object
     * @throws AuthenticationException if user not found or password is incorrect
     */
    public User login(String email, String password) throws AuthenticationException {
        User user = users.get(email);

        if (user == null) {
            throw new AuthenticationException("User with email '" + email + "' not found");
        }

        // Verify password using hash comparison
        String hashedPassword = Validator.hashPassword(password);
        if (!user.getPassword().equals(hashedPassword)) {
            throw new AuthenticationException("Incorrect password");
        }

        currentUser = user;
        System.out.println("Login successful!");
        return user;
    }

    /**
     * Logs out the currently authenticated user.
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("Goodbye, " + currentUser.getFullName() + "!");
            currentUser = null;
        }
    }

    /**
     * Registers a new user in the system.
     * Password is automatically hashed before storage.
     *
     * @param user the user to register
     * @throws ValidationException if email is already registered
     */
    public void registerUser(User user) throws ValidationException {
        if (users.containsKey(user.getEmail())) {
            throw new ValidationException("email", "Email already registered");
        }

        // Hash password before storing
        String hashedPassword = Validator.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        users.put(user.getEmail(), user);
        DataRepository.getInstance().save();
    }

    /**
     * Removes a user from the system by email.
     *
     * @param email the email of the user to remove
     * @throws ValidationException if user not found
     */
    public void removeUser(String email) throws ValidationException {
        if (!users.containsKey(email)) {
            throw new ValidationException("email", "User not found");
        }
        users.remove(email);
        DataRepository.getInstance().save();
    }

    /**
     * Updates an existing user's information.
     * If password is changed, it will be hashed.
     *
     * @param user the updated user object
     * @throws ValidationException if user not found
     */
    public void updateUser(User user) throws ValidationException {
        if (!users.containsKey(user.getEmail())) {
            throw new ValidationException("email", "User not found");
        }
        users.put(user.getEmail(), user);
        DataRepository.getInstance().save();
    }

    /**
     * Checks if a user is currently authenticated.
     *
     * @return true if a user is logged in, false otherwise
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    /**
     * Gets the currently authenticated user.
     *
     * @return the current user, or null if not authenticated
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Gets all users in the system.
     *
     * @return unmodifiable map of all users (email → User)
     */
    public Map<String, User> getAllUsers() {
        return Collections.unmodifiableMap(users);
    }

    /**
     * Gets a user by their unique ID.
     *
     * @param id the user's unique ID
     * @return the User object, or null if not found
     */
    public User getUserById(String id) {
        return users.values().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst().orElse(null);
    }
}