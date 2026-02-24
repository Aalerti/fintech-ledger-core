package bank.app.springbootbankapp.repository;

import bank.app.springbootbankapp.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account,Long> {
    Account findByNumber(String number);
}
