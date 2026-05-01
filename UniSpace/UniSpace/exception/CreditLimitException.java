package UniSpace.exception;

public class CreditLimitException extends CourseRegistrationException {
    public CreditLimitException(int current, int max) {
        super("Credit limit exceeded: current=" + current + ", max=" + max);
    }
}
