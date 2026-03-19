package bank.app.springbootbankapp.service;

import bank.app.springbootbankapp.dto.TransferRequestDto;
import bank.app.springbootbankapp.dto.TransferResponseDto;
import bank.app.springbootbankapp.entity.Account;
import bank.app.springbootbankapp.entity.Transaction;
import bank.app.springbootbankapp.entity.User;
import bank.app.springbootbankapp.exception.AccountNotFoundException;
import bank.app.springbootbankapp.exception.AccountsHaveDifferentCurrency;
import bank.app.springbootbankapp.exception.ForbiddenOperationException;
import bank.app.springbootbankapp.mapper.TransferMapper;
import bank.app.springbootbankapp.repository.AccountRepository;
import bank.app.springbootbankapp.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferMapper transferMapper;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public TransferResponseDto transfer(TransferRequestDto transferRequestDto, User currentUser, String idempotencyKey) {

        Optional<String> cached = idempotencyService.getCachedResponse(idempotencyKey);
        if (cached.isPresent()) {
            try {
                return objectMapper.readValue(cached.get(), TransferResponseDto.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize cached idempotency response", e);
            }
        }

        idempotencyService.markProcessing(idempotencyKey);

        
        TransferResponseDto responseDto = executeTransfer(transferRequestDto, currentUser);

        
        try {
            String responseJson = objectMapper.writeValueAsString(responseDto);
            idempotencyService.saveResponse(idempotencyKey, responseJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize idempotency response", e);
        }

        return responseDto;
    }

    private TransferResponseDto executeTransfer(TransferRequestDto transferRequestDto, User currentUser) {
        long fromId = transferRequestDto.getFromId();
        long toId = transferRequestDto.getToId();
        BigDecimal amount = transferRequestDto.getAmount();

        if (fromId == toId) {
            throw new IllegalArgumentException("Transfer to the same account is not allowed");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount in transaction must be greater than 0");
        }

        Account accountFrom = accountRepository
                .findById(fromId)
                .orElseThrow(() -> new AccountNotFoundException("Account with " + fromId + " not found"));

        if (accountFrom.getUser().getId() != currentUser.getId()) {
            throw new ForbiddenOperationException("You can't transfer money from someone else's account!");
        }

        Account accountTo = accountRepository
                .findById(toId)
                .orElseThrow(() -> new AccountNotFoundException("Account with " + toId + " not found"));

        if (accountFrom.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Balance is less than amount of transaction");
        }

        if (!accountFrom.getCurrency().equals(accountTo.getCurrency())) {
            throw new AccountsHaveDifferentCurrency("Accounts have different currency");
        }

        accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
        accountTo.setBalance(accountTo.getBalance().add(amount));

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setCurrency(accountFrom.getCurrency());
        transaction.setStatus("TRANSFER");
        transaction.setAccountFrom(accountFrom);
        transaction.setAccountTo(accountTo);

        transaction = transactionRepository.save(transaction);

        return transferMapper.toResponseDto(transaction);
    }
}
