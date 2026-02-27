package bank.app.springbootbankapp.exception_handling;

import bank.app.springbootbankapp.exception.AccountNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class TransferGlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<AccountIncorrectData> handleAccountNotFoundException(AccountNotFoundException e) {
        AccountIncorrectData accountIncorrectData = new AccountIncorrectData();
        accountIncorrectData.setInfo(e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(accountIncorrectData);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<TransferIncorrectData> handleIllegalArgumentException(IllegalArgumentException e) {
        TransferIncorrectData transferIncorrectData = new TransferIncorrectData();
        transferIncorrectData.setInfo(e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(transferIncorrectData);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
