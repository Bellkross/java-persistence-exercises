package ua.procamp.locking.exception;

public class OptimisticLockingException extends RuntimeException {

    public OptimisticLockingException(final String message) {
        super(message);
    }

    public OptimisticLockingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
