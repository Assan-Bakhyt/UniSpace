package UniSpace.exception;

import UniSpace.model.user.Student;

public class MaxFailsException extends CourseRegistrationException {

    public MaxFailsException(String courseCode) {
        super(String.format(
                "Cannot register for course '%s': maximum of %d failed attempts reached.",
                courseCode, Student.MAX_FAILS
        ));
    }
}
