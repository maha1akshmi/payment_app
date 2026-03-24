package com.payment.backend.repository;

import java.util.List;

import com.payment.backend.entity.User;

public interface TransactionRepositorypository<Transaction, Long> {

    List<Transaction> findBySenderOrReceiverOrderByTimestampDesc(User sender,User receiver);

    List<Transaction> findBySenderOrReceiverOrderByTimestampDesc1(User sender);
     List<Transaction> findBySenderOrReceiverOrderByTimestampDesc(User receiver);
}
