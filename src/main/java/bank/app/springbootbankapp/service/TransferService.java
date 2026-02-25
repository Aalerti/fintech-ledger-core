package bank.app.springbootbankapp.service;

import bank.app.springbootbankapp.entity.Account;

import java.math.BigDecimal;

public interface TransferService {

    void transfer(long fromId, long toId, BigDecimal amount);
}
