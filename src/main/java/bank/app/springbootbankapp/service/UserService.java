package bank.app.springbootbankapp.service;

import bank.app.springbootbankapp.dto.LoginRequestDto;
import bank.app.springbootbankapp.dto.LoginResponseDto;
import bank.app.springbootbankapp.entity.User;

public interface UserService {

    User findByUsername(String username);

    LoginResponseDto login(LoginRequestDto loginRequestDto);
}
