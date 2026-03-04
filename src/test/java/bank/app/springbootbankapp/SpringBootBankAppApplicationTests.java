package bank.app.springbootbankapp;

import bank.app.springbootbankapp.dto.TransferRequestDto;
import bank.app.springbootbankapp.entity.Account;
import bank.app.springbootbankapp.exception.AccountNotFoundException;
import bank.app.springbootbankapp.repository.AccountRepository;
import bank.app.springbootbankapp.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SpringBootBankAppApplicationTests {

    @Autowired
    TransferService transferService;

    @Autowired
    AccountRepository accountRepository;

    @Test
    void testOptimisticLockingOnConcurrentTransfers() throws InterruptedException {
        TransferRequestDto request =  new TransferRequestDto();
        request.setFromId(1l);
        request.setToId(2l);
        request.setAmount(new BigDecimal("100.00"));

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyThreads = new  CountDownLatch(threadCount);
        CountDownLatch startLatch = new  CountDownLatch(1);
        CountDownLatch downLatch = new  CountDownLatch(threadCount);

        AtomicInteger  successCount = new AtomicInteger(0);
        AtomicInteger  failureCount = new AtomicInteger(0);

        Account accountBefore = accountRepository.findById(1l).orElseThrow(
                () -> new AccountNotFoundException("Account don't exist"));
        BigDecimal balanceBefore = accountBefore.getBalance();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyThreads.countDown();
                    startLatch.await();


                    transferService.transfer(request);
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


        Account accountAfter = accountRepository.findById(1l).orElseThrow(
                () -> new AccountNotFoundException("Account don't exist"));
        BigDecimal balanceAfter = accountAfter.getBalance();

        System.out.println("Успешных транзакций: " + successCount.get());
        System.out.println("Заблокированных транзакций: " + failureCount.get());
        System.out.println("Начальный баланс: " + balanceBefore);
        System.out.println("Конечный баланс: " + balanceAfter);

        assertTrue(failureCount.get() > 0, "Ожидалось, " +
                "что параллельные транзакции упадут с OptimisticLockingFailureException");

        BigDecimal expectedBalance = balanceBefore.subtract(new BigDecimal("100.00").multiply(new BigDecimal(successCount.get())));
        assertEquals(0, expectedBalance.compareTo(balanceAfter),
                "Баланс в бд не совпадает с расчетным, произошло двойное списание0");


    }

}
