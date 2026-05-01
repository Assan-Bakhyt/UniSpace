package UniSpace.exception;

public class CreditLimitException extends CourseRegistrationException {
    private static final int MAX_CREDITS = 21;

    public CreditLimitException(int currentCredits, int attemptedCredits) {
        super(String.format(
                "Credit limit exceeded: cannot add %d credits. Current: %d, Max allowed: %d.",
                attemptedCredits, currentCredits, MAX_CREDITS
        ));
    }
}
