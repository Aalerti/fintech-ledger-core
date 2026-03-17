package bank.app.springbootbankapp.service;

import bank.app.springbootbankapp.dto.TransactionDto;
import bank.app.springbootbankapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {

    Page<TransactionDto> getTransactionsByAccountId(Long accountId, Pageable pageable, User currUser);
}
