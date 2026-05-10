package UniSpace.util;

public class Validator {

    public static boolean validateEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    public static boolean validatePassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean validateScore(double score, double max) {
        return score >= 0 && score <= max;
    }
}
