package com.fintech.backend.repository;

import com.fintech.backend.dto.TransactionsDto;
import com.fintech.backend.models.Transactions;
import com.fintech.backend.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, String> {
    Optional<Transactions> findByTransactionId(String transactionId);

    List<Transactions> findAllByUserId(Users userId);

    Transactions findByUserId(Users userId);

    List<Transactions> findAllByUserIdAndDateBetween(Users userId, LocalDate dateAfter, LocalDate dateBefore);
}
