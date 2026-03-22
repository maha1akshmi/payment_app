package com.payment.backend.repository;

import com.payment.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySenderOrReceiverOrderByTimestampDesc(
            User sender, User receiver);

    List<Transaction> findBySenderOrderByTimestampDesc(User sender);

    List<Transaction> findByReceiverOrderByTimestampDesc(User receiver);
}