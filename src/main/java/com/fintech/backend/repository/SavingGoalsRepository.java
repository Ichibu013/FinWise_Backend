package com.fintech.backend.repository;

import com.fintech.backend.models.SavingGoals;
import com.fintech.backend.utils.enums.Status;
import com.fintech.backend.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SavingGoalsRepository extends JpaRepository<SavingGoals, Long> {
    SavingGoals findByCompletionDateBeforeAndUserId(LocalDate completionDateBefore, Users userId);

    List<SavingGoals> findAllByCompletionDateBefore(LocalDate completionDateBefore);

    SavingGoals findByStatus(Status status);

    SavingGoals findByStatusAndUserId(Status status, Users userId);

    List<SavingGoals> findAllByUserIdAndCompletionDateBetween(Users userId, LocalDate completionDateAfter, LocalDate completionDateBefore);
}
