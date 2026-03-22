package com.payment.backend.repository;


import com.payment.backend.entity.Account;
import com.payment.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUser(User user);

    Optional<Account> findByAccountNumber(String accountNumber);
}
