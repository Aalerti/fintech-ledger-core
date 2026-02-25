package bank.app.springbootbankapp.service;

import bank.app.springbootbankapp.entity.Account;
import bank.app.springbootbankapp.entity.Transaction;

import java.math.BigDecimal;

public interface TransferService {

    Transaction transfer(long fromId, long toId, BigDecimal amount);
}
