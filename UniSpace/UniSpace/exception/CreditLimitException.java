package UniSpace.exception;

public class CreditLimitException extends RuntimeException {
    public CreditLimitException(String message) {
        super(message);
    }
}
