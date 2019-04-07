package ua.procamp.locking.exception;

public class PessimisticLockingException extends RuntimeException {

    public PessimisticLockingException(final String message) {
        super(message);
    }

    public PessimisticLockingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
