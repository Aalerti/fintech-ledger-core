package bank.app.springbootbankapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean checkIdempotency(String key) {
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, "PROCESSING", Duration.ofHours(24));
        return Boolean.TRUE.equals(result);
    }

}
