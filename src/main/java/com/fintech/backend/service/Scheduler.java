package com.fintech.backend.service;

import com.fintech.backend.models.SavingGoals;
import com.fintech.backend.utils.enums.Status;
import com.fintech.backend.models.Users;
import com.fintech.backend.repository.SavingGoalsRepository;
import com.fintech.backend.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Scheduler {
    private final GoalsService goalsService;
    private final UsersRepository usersRepository;
    private final SavingGoalsRepository savingGoalsRepository;

    /**
     * Scheduled job that runs at 00:00 on the first day of each month to create
     * a new monthly saving goal for every user.
     *
     * <p>The goal name follows the pattern "MMMM yyyy Saving Goal" and the completion
     * date is set to one month after creation.</p>
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void createMonthlySavingGoals() {
        List<Users> userIds = usersRepository.findAll();
        LocalDate today = LocalDate.now();
        String goalName = today.format(DateTimeFormatter.ofPattern("MMMM yyyy")) + " Saving Goal";
        LocalDate completionDate = today.plusMonths(1);

        for (Users user : userIds) {
            try {
                goalsService.createSavingGoalForUser(user, goalName, completionDate);
            } catch (Exception e) {
                log.error("Failed to create goal for user: {}. Error: {}", user.getUserId(), e.getMessage());
            }
        }
    }

    /**
     * Scheduled job that runs at 00:00 on the last day of each month to update
     * the status of saving goals whose completion date has passed.
     *
     * <p>If a goal's current balance is greater than or equal to its goal amount,
     * the status is set to {@code COMPLETED}; otherwise, it is set to {@code ON_HOLD}.</p>
     */
    @Transactional
    @Scheduled(cron = "0 0 0 L * ?")
    public void updateExpiredSavingGoals() {
        List<SavingGoals> expiredGoals = savingGoalsRepository.findAllByCompletionDateBefore(LocalDate.now());

        log.info("Starting cleanup for {} expired saving goals.", expiredGoals.size());

        for (SavingGoals goal : expiredGoals) {
            try {
                if (goal.getGoalAmount().compareTo(goal.getCurrentBalance()) <= 0) {
                    goal.setStatus(Status.COMPLETED);
                    savingGoalsRepository.save(goal);
                    log.info("Goal ID {} COMPLETED for user: {}", goal.getGoalId(), goal.getUserId());
                } else {
                    goal.setStatus(Status.ON_HOLD);
                    savingGoalsRepository.save(goal);
                    log.warn("Goal ID {} ON_HOLD (expired/unfunded) for user: {}", goal.getGoalId(), goal.getUserId());
                }
            } catch (Exception e) {
                log.error("Failed to update goal status for Goal ID {}. Error: {}", goal.getGoalId(), e.getMessage(), e);
            }
        }

        log.info("Finished updating expired saving goals.");
    }
}
