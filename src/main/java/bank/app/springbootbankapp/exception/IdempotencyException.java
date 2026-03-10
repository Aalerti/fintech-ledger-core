package bank.app.springbootbankapp.exception;

public class IdempotencyException extends RuntimeException {
    public IdempotencyException(String message) {
        super(message);
    }
}
