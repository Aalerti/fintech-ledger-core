package bank.app.springbootbankapp.service;

import bank.app.springbootbankapp.entity.Account;
import bank.app.springbootbankapp.entity.Transaction;
import bank.app.springbootbankapp.exception.AccountNotFoundException;
import bank.app.springbootbankapp.exception.AccountsHaveDifferentCurrency;
import bank.app.springbootbankapp.repository.AccountRepository;
import bank.app.springbootbankapp.repository.TransactionRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;


    @Transactional
    @Override
    public void transfer(long fromId, long toId, BigDecimal amount) {

        if (fromId == toId) {
            throw new IllegalArgumentException("Transfer to the same account is not allowed");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw  new IllegalArgumentException("Amount in transaction must be greater than 0");
        }

        Account accountFrom = accountRepository
                .findById(fromId)
                .orElseThrow(() -> new AccountNotFoundException("Account with" + fromId + " not found"));

        Account accountTo = accountRepository
                .findById(toId)
                .orElseThrow(() -> new AccountNotFoundException("Account with" + toId + " not found"));


        if (accountFrom.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Balance in transaction is less than amount of transaction");
        }

        if (!accountFrom.getCurrency().equals(accountTo.getCurrency())) {
            throw new AccountsHaveDifferentCurrency("Account have different currency");
        }

        accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
        accountTo.setBalance(accountTo.getBalance().add(amount));


        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setCurrency(accountFrom.getCurrency());
        transaction.setStatus("TRANSFER");
        transaction.setAccountFrom(accountFrom);
        transaction.setAccountTo(accountTo);

        transactionRepository.save(transaction);
    }
}
