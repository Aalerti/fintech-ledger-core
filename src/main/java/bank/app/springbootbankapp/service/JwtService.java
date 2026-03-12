package bank.app.springbootbankapp.service;

import bank.app.springbootbankapp.entity.User;

public interface JwtService {
    String generateToken(User user);

    String extractUsername(String token);
}
