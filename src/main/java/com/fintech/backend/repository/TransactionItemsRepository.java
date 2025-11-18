package com.fintech.backend.repository;

import com.fintech.backend.models.TransactionItems;
import com.fintech.backend.models.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionItemsRepository extends JpaRepository<TransactionItems, Long> {
    List<TransactionItems> findAllByTransactionId(Transactions transactionId);
}
