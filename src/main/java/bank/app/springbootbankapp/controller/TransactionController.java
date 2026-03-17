package bank.app.springbootbankapp.controller;

import bank.app.springbootbankapp.dto.TransactionDto;
import bank.app.springbootbankapp.entity.User;
import bank.app.springbootbankapp.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<Page<TransactionDto>> getAllTransactions(
            @PathVariable Long id,
            Pageable pageable,
            @AuthenticationPrincipal User currUser) {
        Page<TransactionDto> transactions = transactionService.getTransactionsByAccountId(id, pageable, currUser);
        return ResponseEntity.ok(transactions);
    }
}
