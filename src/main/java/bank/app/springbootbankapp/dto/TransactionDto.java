package bank.app.springbootbankapp.dto;

import bank.app.springbootbankapp.entity.Currency;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class TransactionDto {
    private long id;

    private BigDecimal amount;
    private Currency currency;
    private String status;
    private LocalDateTime createdAt;

    private String accountFromNumber;
    private String accountToNumber;
}
