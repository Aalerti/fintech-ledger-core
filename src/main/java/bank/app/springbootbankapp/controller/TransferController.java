package bank.app.springbootbankapp.controller;

import bank.app.springbootbankapp.dto.TransferRequestDto;
import bank.app.springbootbankapp.dto.TransferResponseDto;
import bank.app.springbootbankapp.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponseDto> transfers(@Valid @RequestBody TransferRequestDto transferRequestDto) {
        TransferResponseDto responseDto = transferService.transfer(transferRequestDto.getFromId(),
                transferRequestDto.getToId(),
                transferRequestDto.getAmount());

        return ResponseEntity.ok(responseDto);
    }
}
