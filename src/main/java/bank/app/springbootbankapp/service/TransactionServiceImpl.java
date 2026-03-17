package bank.app.springbootbankapp.service;

import bank.app.springbootbankapp.dto.TransactionDto;
import bank.app.springbootbankapp.entity.Account;
import bank.app.springbootbankapp.entity.User;
import bank.app.springbootbankapp.exception.AccountNotFoundException;
import bank.app.springbootbankapp.mapper.TransactionMapper;
import bank.app.springbootbankapp.repository.AccountRepository;
import bank.app.springbootbankapp.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    @Override
    public Page<TransactionDto> getTransactionsByAccountId(Long accountId, Pageable pageable, User currUser) {

        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account with" + accountId + " not found"));

        if (account.getUser().getId() != currUser.getId()) {
            throw new IllegalArgumentException("You can't see transactions from someone else's account!");
        }

        return transactionRepository
                .findAllByAccountFromIdOrAccountToId(accountId, accountId, pageable)
                .map(transactionMapper::toTransactionDto);
    }
}
