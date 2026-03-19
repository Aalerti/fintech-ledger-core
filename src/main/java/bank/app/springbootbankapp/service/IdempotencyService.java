package bank.app.springbootbankapp.service;

import java.util.Optional;

public interface IdempotencyService {

    Optional<String> getCachedResponse(String key);

    void markProcessing(String key);

    void saveResponse(String key, String responseJson);
}
