package com.fintech.backend.repository;

import com.fintech.backend.dto.SavingRecordsDto;
import com.fintech.backend.models.Category;
import com.fintech.backend.models.SavingRecords;
import com.fintech.backend.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingRecordsRepository extends JpaRepository<SavingRecords, Long> {
    List<SavingRecords> findAllByGoalId_UserIdAndCategoryGoalId_CategoryId(Users userById, Category category);
}
