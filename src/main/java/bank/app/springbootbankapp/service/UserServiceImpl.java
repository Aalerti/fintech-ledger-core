package bank.app.springbootbankapp.service;

import bank.app.springbootbankapp.dto.LoginRequestDto;
import bank.app.springbootbankapp.dto.LoginResponseDto;
import bank.app.springbootbankapp.entity.User;
import bank.app.springbootbankapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    @Override
    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return user;
    }

    @Transactional
    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        User user = findByUsername(loginRequestDto.getUsername());

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect password");
        }
        LoginResponseDto loginResponseDto = new LoginResponseDto();
        loginResponseDto.setToken(jwtService.generateToken(user));
        return loginResponseDto;
    }
}
