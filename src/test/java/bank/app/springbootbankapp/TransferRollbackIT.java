package bank.app.springbootbankapp;

import bank.app.springbootbankapp.dto.LoginRequestDto;
import bank.app.springbootbankapp.dto.LoginResponseDto;
import bank.app.springbootbankapp.dto.TransferRequestDto;
import bank.app.springbootbankapp.entity.Account;
import bank.app.springbootbankapp.entity.Bank;
import bank.app.springbootbankapp.entity.Currency;
import bank.app.springbootbankapp.entity.User;
import bank.app.springbootbankapp.exception.AccountNotFoundException;
import bank.app.springbootbankapp.repository.AccountRepository;
import bank.app.springbootbankapp.repository.BankRepository;
import bank.app.springbootbankapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferRollbackIT extends AbstractTestcontainerBase {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String aliceJwt;
    private long aliceAccountId;
    private long rubleAccountId;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        User alice = userRepository.findByUsername("alice_dev")
                .orElseThrow(() -> new UsernameNotFoundException("alice_dev not found"));
        alice.setPasswordHash(passwordEncoder.encode("password123"));
        userRepository.save(alice);

        aliceJwt = loginAndGetToken("alice_dev", "password123");

        aliceAccountId = accountRepository.findByNumber("ACC-1001")
                .orElseThrow(() -> new AccountNotFoundException("ACC-1001 not found")).getId();

        
        User bob = userRepository.findByUsername("bob_dev")
                .orElseThrow(() -> new UsernameNotFoundException("bob_dev not found"));
        Bank bank = bankRepository.findAll().get(0);

        Account rubleAccount = new Account();
        rubleAccount.setBalance(new BigDecimal("9999.00"));
        rubleAccount.setCurrency(Currency.RUBLE);
        rubleAccount.setNumber("RUBLE-TEST-" + UUID.randomUUID());
        rubleAccount.setUser(bob);
        rubleAccount.setBank(bank);

        rubleAccountId = accountRepository.save(rubleAccount).getId();
    }

    @Test
    void transferWithCurrencyMismatch_shouldRollbackAndLeaveSenderBalanceUnchanged() {
        BigDecimal balanceBefore = accountRepository.findById(aliceAccountId)
                .orElseThrow().getBalance();

        TransferRequestDto request = new TransferRequestDto();
        request.setFromId(aliceAccountId);
        request.setToId(rubleAccountId);
        request.setAmount(new BigDecimal("50.00"));

        
        webTestClient.post()
                .uri("/api/transfers")
                .header("Authorization", "Bearer " + aliceJwt)
                .header("Idempotency-Key", "rollback-test-" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();

        
        BigDecimal balanceAfter = accountRepository.findById(aliceAccountId)
                .orElseThrow().getBalance();

        assertThat(balanceAfter)
                .as("Sender balance must be unchanged after a rolled-back transaction")
                .isEqualByComparingTo(balanceBefore);
    }

    

    private String loginAndGetToken(String username, String password) {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        return webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponseDto.class)
                .returnResult()
                .getResponseBody()
                .getToken();
    }
}

