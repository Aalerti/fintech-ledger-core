package bank.app.springbootbankapp.dto;

import bank.app.springbootbankapp.entity.Currency;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TransactionDto {
    private long id;

    private BigDecimal amount;
    private Currency currency;
    private String status;
    private LocalDateTime createdAt;

    private String accountFromNumber;
    private String accountToNumber;
}
