package bank.app.springbootbankapp;

import bank.app.springbootbankapp.dto.LoginRequestDto;
import bank.app.springbootbankapp.dto.LoginResponseDto;
import bank.app.springbootbankapp.dto.TransferRequestDto;
import bank.app.springbootbankapp.dto.TransferResponseDto;
import bank.app.springbootbankapp.exception.AccountNotFoundException;
import bank.app.springbootbankapp.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferIdempotencyIT extends AbstractTestcontainerBase {

        @LocalServerPort
        private int port;

        private WebTestClient webTestClient;

        @org.springframework.beans.factory.annotation.Autowired
        private AccountRepository accountRepository;

        @Autowired
        private bank.app.springbootbankapp.repository.UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private String aliceJwt;
        private long aliceAccountId;
        private long bobAccountId;

        @BeforeEach
        void setUp() {
                webTestClient = WebTestClient.bindToServer()
                                .baseUrl("http://localhost:" + port)
                                .build();

                bank.app.springbootbankapp.entity.User alice = userRepository.findByUsername("alice_dev")
                                .orElseThrow(() -> new RuntimeException("alice_dev not found"));
                alice.setPasswordHash(passwordEncoder.encode("password123"));
                userRepository.save(alice);

                aliceJwt = loginAndGetToken("alice_dev", "password123");
                aliceAccountId = accountRepository.findByNumber("ACC-1001")
                                .orElseThrow(() -> new AccountNotFoundException("ACC-1001 not found")).getId();
                bobAccountId = accountRepository.findByNumber("ACC-1002")
                                .orElseThrow(() -> new AccountNotFoundException("ACC-1002 not found")).getId();
        }

        @Test
        void duplicateIdempotencyKey_shouldReturnSameResponseWithoutDoubleTransfer() {
                String idempotencyKey = "idempotency-test-" + UUID.randomUUID();
                BigDecimal transferAmount = new BigDecimal("100.00");

                BigDecimal balanceBefore = accountRepository.findByNumber("ACC-1001")
                                .orElseThrow().getBalance();

                TransferRequestDto request = new TransferRequestDto();
                request.setFromId(aliceAccountId);
                request.setToId(bobAccountId);
                request.setAmount(transferAmount);

                
                TransferResponseDto firstBody = doTransfer(request, aliceJwt, idempotencyKey);
                assertThat(firstBody).isNotNull();
                Long firstTransactionId = firstBody.getId();

                
                TransferResponseDto secondBody = doTransfer(request, aliceJwt, idempotencyKey);
                assertThat(secondBody).isNotNull();
                Long secondTransactionId = secondBody.getId();

                
                assertThat(secondTransactionId)
                                .as("Second request must return the same cached transaction ID")
                                .isEqualTo(firstTransactionId);

                
                BigDecimal balanceAfter = accountRepository.findByNumber("ACC-1001")
                                .orElseThrow().getBalance();
                BigDecimal expectedBalance = balanceBefore.subtract(transferAmount);
                assertThat(balanceAfter)
                                .as("Balance must decrease by only one transfer amount")
                                .isEqualByComparingTo(expectedBalance);
        }

        

        private TransferResponseDto doTransfer(TransferRequestDto body, String jwt, String idempotencyKey) {
                return webTestClient.post()
                                .uri("/api/transfers")
                                .header("Authorization", "Bearer " + jwt)
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(body)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(TransferResponseDto.class)
                                .returnResult()
                                .getResponseBody();
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
