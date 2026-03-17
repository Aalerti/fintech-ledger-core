package bank.app.springbootbankapp;

import bank.app.springbootbankapp.dto.TransferRequestDto;
import bank.app.springbootbankapp.entity.Account;
import bank.app.springbootbankapp.entity.User;
import bank.app.springbootbankapp.exception.AccountNotFoundException;
import bank.app.springbootbankapp.repository.AccountRepository;
import bank.app.springbootbankapp.repository.UserRepository;
import bank.app.springbootbankapp.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SpringBootBankAppApplicationTests extends AbstractTestcontainerBase {

    @Autowired
    TransferService transferService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void testOptimisticLockingOnConcurrentTransfers() throws InterruptedException {
        User currUser = userRepository.findByUsername("alice_dev")
                .orElseThrow(() -> new UsernameNotFoundException("User alice_dev don't exist"));

        Account accountBefore = accountRepository.findByNumber("ACC-1001")
                .orElseThrow(() -> new AccountNotFoundException("Account ACC-1001 don't exist"));

        Account accountTo = accountRepository.findByNumber("ACC-1002")
                .orElseThrow(() -> new AccountNotFoundException("Account ACC-1002 don't exist"));


        TransferRequestDto request =  new TransferRequestDto();
        request.setFromId(accountBefore.getId());
        request.setToId(accountTo.getId());
        request.setAmount(new BigDecimal("100.00"));

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyThreads = new  CountDownLatch(threadCount);
        CountDownLatch startLatch = new  CountDownLatch(1);
        CountDownLatch downLatch = new  CountDownLatch(threadCount);

        AtomicInteger  successCount = new AtomicInteger(0);
        AtomicInteger  failureCount = new AtomicInteger(0);

        BigDecimal balanceBefore = accountBefore.getBalance();


        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyThreads.countDown();
                    startLatch.await();


                    transferService.transfer(request, currUser);
                    successCount.incrementAndGet();

                } catch (ObjectOptimisticLockingFailureException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Другая ошибка: " + e.getMessage());
                } finally {
                    downLatch.countDown();
                }
            });
        }

        readyThreads.await();
        startLatch.countDown();
        downLatch.await();


        Account accountAfter = accountRepository.findByNumber("ACC-1001")
                .orElseThrow(() -> new AccountNotFoundException("Account don't exist"));
        BigDecimal balanceAfter = accountAfter.getBalance();

        System.out.println("Успешных транзакций: " + successCount.get());
        System.out.println("Заблокированных транзакций: " + failureCount.get());
        System.out.println("Начальный баланс: " + balanceBefore);
        System.out.println("Конечный баланс: " + balanceAfter);

        assertTrue(failureCount.get() > 0, "Ожидалось, " +
                "что параллельные транзакции упадут с OptimisticLockingFailureException");

        BigDecimal expectedBalance = balanceBefore.subtract(new BigDecimal("100.00").multiply(new BigDecimal(successCount.get())));
        assertEquals(0, expectedBalance.compareTo(balanceAfter),
                "Баланс в бд не совпадает с расчетным, произошло двойное списание");

    }

}
