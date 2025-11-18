package com.fintech.backend.repository;

import com.fintech.backend.dto.CategoryGoalIdDto;
import com.fintech.backend.models.Category;
import com.fintech.backend.models.GoalCategories;
import com.fintech.backend.models.SavingGoals;
import com.fintech.backend.models.Users;
import com.fintech.backend.utils.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalCategoriesRepository extends JpaRepository<GoalCategories, Long> {
    Optional<GoalCategories> findByGoalCategoryId(Long goalCategoryId);

    GoalCategories findByCategoryIdAndGoalId_UserId(Category categoryId, Users goalIdUserId);

    List<GoalCategories> findAllByCategoryIdAndGoalId_UserId(Category category, Users userById);

    List<GoalCategories> findAllByGoalId_UserId(Users user);

    List<GoalCategories> findAllByCategoryIdAndGoalId(Category category, SavingGoals goal);

    GoalCategories findByCategoryIdAndGoalId_UserIdAndGoalId_Status(Category category, Users user, Status status);
}
