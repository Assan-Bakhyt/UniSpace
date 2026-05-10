package UniSpace.util;

/**
 * Utility class for validating user input data.
 * Provides static methods for common validation tasks like email, password, and score validation.
 */
public class Validator {

    /**
     * Validates an email address format.
     * Checks if the email is not null and contains both '@' and '.' characters.
     *
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static boolean validateEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    /**
     * Validates a password.
     * Checks if the password is not null and has at least 6 characters.
     *
     * @param password the password to validate
     * @return true if the password is valid, false otherwise
     */
    public static boolean validatePassword(String password) {
        if (password == null || password.length() < 6) return false;
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }

    /**
     * Validates a score value.
     * Checks if the score is within the acceptable range [0, max].
     *
     * @param score the score to validate
     * @param max   the maximum allowed score
     * @return true if the score is valid, false otherwise
     */
    public static boolean validateScore(double score, double max) {
        return score >= 0 && score <= max;
    }

    /**
     * Hashes a password using SHA-256 algorithm.
     * This method provides basic password hashing for security purposes.
     *
     * @param password the plain text password to hash
     * @return the hashed password as a hexadecimal string, or null if hashing fails
     */
    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifies a plain text password against a hashed password.
     *
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the stored hashed password
     * @return true if the passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        String hashedInput = hashPassword(plainPassword);
        return hashedInput != null && hashedInput.equals(hashedPassword);
    }
}