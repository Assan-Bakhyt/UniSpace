package UniSpace.exception;

<<<<<<< HEAD
public class CreditLimitException extends CourseRegistrationException {
    private static final int MAX_CREDITS = 21;

    public CreditLimitException(int currentCredits, int attemptedCredits) {
        super(String.format(
                "Credit limit exceeded: cannot add %d credits. Current: %d, Max allowed: %d.",
                attemptedCredits, currentCredits, MAX_CREDITS
        ));
=======
public class CreditLimitException extends Exception {
    public CreditLimitException(int current, int max) {
        super("Credit limit exceeded: " + current + "/" + max);
>>>>>>> origin/main
    }
}
