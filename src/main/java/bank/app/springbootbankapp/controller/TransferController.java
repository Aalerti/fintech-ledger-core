package bank.app.springbootbankapp.controller;

import bank.app.springbootbankapp.dto.TransferRequestDto;
import bank.app.springbootbankapp.dto.TransferResponseDto;
import bank.app.springbootbankapp.entity.User;
import bank.app.springbootbankapp.exception.IdempotencyException;
import bank.app.springbootbankapp.service.IdempotencyService;
import bank.app.springbootbankapp.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    private final IdempotencyService idempotencyService;

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponseDto> transfers(
            @Valid @RequestBody TransferRequestDto transferRequestDto,
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @AuthenticationPrincipal User currentUser
    ) {
        if (!idempotencyService.checkIdempotency(idempotencyKey)) {
            throw new IdempotencyException("Transfer with this idempotency key is already exist");
        }
        TransferResponseDto responseDto = transferService.transfer(transferRequestDto, currentUser);

        return ResponseEntity.ok(responseDto);
    }
}
