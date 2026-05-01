package UniSpace.exception;

public class CreditLimitException extends Exception {
    public CreditLimitException(int current, int max) {
        super("Credit limit exceeded: " + current + "/" + max);
    }
}
