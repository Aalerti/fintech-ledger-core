package bank.app.springbootbankapp.service;

import bank.app.springbootbankapp.dto.TransferRequestDto;
import bank.app.springbootbankapp.dto.TransferResponseDto;
import bank.app.springbootbankapp.entity.User;


public interface TransferService {

    TransferResponseDto transfer(TransferRequestDto transferRequestDto, User currentUser, String idempotencyKey);
}
