package bank.app.springbootbankapp.dto;

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
