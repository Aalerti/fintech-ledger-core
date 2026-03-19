package bank.app.springbootbankapp.repository;

import bank.app.springbootbankapp.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, Long> {
}

