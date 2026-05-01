package UniSpace.exception;

public class MaxFailsException extends CourseRegistrationException {
    private static final int MAX_FAILS = 3;

    public MaxFailsException(String courseCode) {
        super(String.format(
                "Cannot register for course '%s': maximum of %d failed attempts reached.",
                courseCode, MAX_FAILS
        ));
    }
}
