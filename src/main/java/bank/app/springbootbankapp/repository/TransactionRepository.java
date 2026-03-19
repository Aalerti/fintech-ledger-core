package bank.app.springbootbankapp.repository;

import bank.app.springbootbankapp.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findAllByAccountFromIdOrAccountToId(Long accountFromId, Long accountToId, Pageable pageable);
}
