package exception;

/**
 Thrown when a supervisor has reached the maximum number of students they can supervise.

 */
public class UserAlreadyExistsException extends Exception {

    /**
     * Creates a new instance of the {@link UserAlreadyExistsException} class with a default error message.
     * The default message is "Model already exists".
     */
    public UserAlreadyExistsException() {
        super("Model already exists");
    }

    /**
     * Creates a new instance of the {@link UserAlreadyExistsException} class with a custom error message.
     *
     * @param message The custom error message to be used.
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
