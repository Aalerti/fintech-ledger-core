package bank.app.springbootbankapp.service;

import bank.app.springbootbankapp.dto.TransferRequestDto;
import bank.app.springbootbankapp.dto.TransferResponseDto;
import bank.app.springbootbankapp.entity.Account;
import bank.app.springbootbankapp.entity.Transaction;

import java.math.BigDecimal;

public interface TransferService {

    TransferResponseDto transfer(TransferRequestDto transferRequestDto);
}
