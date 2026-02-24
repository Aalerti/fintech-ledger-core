package bank.app.springbootbankapp.repository;

import bank.app.springbootbankapp.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Account,Long> {
}
