package bank.app.springbootbankapp.dto;

import bank.app.springbootbankapp.entity.Transaction;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TransferResponseDto {
    private Long id;

    private BigDecimal amount;

    private String status;
}
