package bank.app.springbootbankapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private static final String PROCESSING_MARKER = "PROCESSING";
    private static final Duration PROCESSING_TTL = Duration.ofSeconds(30);
    private static final Duration RESPONSE_TTL = Duration.ofHours(24);

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Optional<String> getCachedResponse(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null || PROCESSING_MARKER.equals(value)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    @Override
    public void markProcessing(String key) {
        stringRedisTemplate.opsForValue().set(key, PROCESSING_MARKER, PROCESSING_TTL);
    }

    @Override
    public void saveResponse(String key, String responseJson) {
        stringRedisTemplate.opsForValue().set(key, responseJson, RESPONSE_TTL);
    }
}
