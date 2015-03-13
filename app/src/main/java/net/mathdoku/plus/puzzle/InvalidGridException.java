package net.mathdoku.plus.puzzle;

public class InvalidGridException extends RuntimeException {
    private static final long serialVersionUID = -1502716956881113470L;

    /**
     * Constructs a new {@code InvalidGridException} that includes the current stack trace.
     */
    public InvalidGridException() {
        super();
    }

    /**
     * Constructs a new {@code InvalidGridException} with the current stack trace and the specified detail message.
     *
     * @param detailMessage
     *         the detail message for this exception.
     */
    public InvalidGridException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code InvalidGridException} with the current stack trace, the specified detail message and the
     * specified cause.
     *
     * @param detailMessage
     *         the detail message for this exception.
     * @param throwable
     *         the cause of this exception.
     */
    public InvalidGridException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@code InvalidGridException} with the current stack trace and the specified cause.
     *
     * @param throwable
     *         the cause of this exception.
     */
    public InvalidGridException(Throwable throwable) {
        super(throwable);
    }
}
