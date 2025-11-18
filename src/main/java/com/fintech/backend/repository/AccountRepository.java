package com.fintech.backend.repository;

import com.fintech.backend.models.Accounts;
import com.fintech.backend.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Accounts, Long> {
    Accounts findByUserId(Users userId);
}
