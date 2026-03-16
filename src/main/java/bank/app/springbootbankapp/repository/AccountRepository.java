package bank.app.springbootbankapp.repository;

import bank.app.springbootbankapp.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByNumber(String number);

}
