package bank.app.springbootbankapp.service;

public interface IdempotencyService {

    boolean checkIdempotency(String key);
}
