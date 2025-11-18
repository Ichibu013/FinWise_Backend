package com.fintech.backend.service;

import com.fintech.backend.config.Exceptions.GoalCategoryNotFoundException;
import com.fintech.backend.dto.CategoryGoalDetailsDto;
import com.fintech.backend.dto.CategoryGoalIdDto;
import com.fintech.backend.dto.CategoryGoalsDto;
import com.fintech.backend.dto.SavingRecordsDto;
import com.fintech.backend.models.*;
import com.fintech.backend.repository.*;
import com.fintech.backend.utils.enums.Status;
import com.fintech.backend.utils.mappers.GenericDtoMapper;
import com.fintech.backend.utils.mappers.GenericResponseFactory;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GoalsService extends BaseService {
    private final GoalCategoriesRepository goalCategoriesRepository;
    private final SavingGoalsRepository savingGoalsRepository;
    private final CategoryRepository categoryRepository;
    private final SavingRecordsRepository savingRecordsRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public GoalsService(GenericDtoMapper mapper,
                        GenericResponseFactory responseFactory,
                        UsersRepository usersRepository,
                        GoalCategoriesRepository goalCategoriesRepository,
                        SavingGoalsRepository savingGoalsRepository,
                        CategoryRepository categoryRepository,
                        SavingRecordsRepository savingRecordsRepository,
                        SimpMessagingTemplate simpMessagingTemplate) {
        super(mapper, responseFactory, usersRepository);
        this.goalCategoriesRepository = goalCategoriesRepository;
        this.savingGoalsRepository = savingGoalsRepository;
        this.categoryRepository = categoryRepository;
        this.savingRecordsRepository = savingRecordsRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    /**
     * Creates a new monthly saving goal for the given user.
     *
     * <p>Initializes the goal with {@link Status#ACTIVE} and persists it.</p>
     *
     * @param user           the owner of the saving goal
     * @param goalName       a human-readable name for the goal (e.g., "November 2025 Saving Goal")
     * @param completionDate the date by which the goal should be completed
     */
    @Transactional
    public void createSavingGoalForUser(Users user, String goalName, LocalDate completionDate) {
        SavingGoals savingGoal = new SavingGoals();
        savingGoal.setUserId(user);
        savingGoal.setGoalName(goalName);
        savingGoal.setCompletionDate(completionDate);
        savingGoal.setStatus(Status.ACTIVE);
        savingGoal.setCurrentBalance(0.0);
        savingGoal.setGoalAmount(0.0);
        savingGoalsRepository.save(savingGoal);
        log.info("{} saving goal created for user: {}", savingGoal.getGoalName(), user.getUserId());
        pushGoalUpdate(user.getUserId());
    }

    @Transactional
    public Double updateSavingGoalForUser(Long userId, Double newGoalAmount) {
        SavingGoals savingGoal = savingGoalsRepository.findByStatusAndUserId(Status.ACTIVE, getUserById(userId));
        if (savingGoal == null) {
            log.error("Saving goal not found for user: {}", userId);
            throw new RuntimeException("Saving goal not found for user: " + userId);
        }
        savingGoal.setGoalAmount(newGoalAmount);
        savingGoalsRepository.save(savingGoal);
        log.info("Saving goal updated for user: {}", userId);
        pushGoalUpdate(userId);
        return newGoalAmount;
    }

    /**
     * Creates a new category goal under the user's active saving goal or updates an existing one.
     *
     * <p>If {@code goalCategoryId} is present inside {@link CategoryGoalsDto}, the existing goal category
     * is updated; otherwise a new goal category is created and linked to the active {@link SavingGoals}.</p>
     *
     * @param userId           the ID of the user who owns the saving goal
     * @param categoryGoalsDto the payload containing category name, budgeted amount, and optional goalCategoryId
     * @return the persisted {@link GoalCategories}, or {@code null} when an error occurs (also logged)
     */
    @Transactional
    public Double createOrUpdateCategoryGoalForUser(Long userId, CategoryGoalsDto categoryGoalsDto) {
        Long goalCategoryId = getCategoryGoalIdFromCategory(categoryGoalsDto.getCategory(), userId) == null
                ? 0
                : getCategoryGoalIdFromCategory(categoryGoalsDto.getCategory(), userId);
        try {
            if (goalCategoryId != 0) {
                // --- Update Logic ---
                return updateExistingCategoryGoal(goalCategoryId, categoryGoalsDto.getBudgetedAmount());
            } else {
                // --- Creation Logic ---
                return createNewCategoryGoal(userId, categoryGoalsDto);
            }
        } catch (GoalCategoryNotFoundException e) {
            log.error("Failed to find goal category with ID {}. Error: {}", goalCategoryId, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Failed to process goal category for user: {}. Error: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves all goal details for a given user and category, including budgeted and saved amounts
     * as well as computed remaining percentage for the period associated with each goal.
     *
     * @param userId       the user's ID
     * @param categoryName the category name to filter by
     * @return a list of {@link CategoryGoalDetailsDto} representing the user's goals in the category
     */
    public List<CategoryGoalDetailsDto> getAllCategoryGoalDetails(Long userId, String categoryName) {

        // 1. Validate Category and User retrieval
        Category category = categoryRepository.findByCategory(categoryName.toUpperCase());
        log.info("Category: {}", category.getCategory());
        if (category == null) {
            category = categoryRepository.findByCategory("OTHER");
        }

        // 2. Fetch all relevant goals
        List<GoalCategories> goalCategories = goalCategoriesRepository
                .findAllByCategoryIdAndGoalId_UserId(category, getUserById(userId));

        log.info("Found {} goal categories for user: {}", goalCategories.size(), userId);

        // 3. Stream and map to DTOs
        return goalCategories.stream()
                .map(goals -> {
                    CategoryGoalDetailsDto detailsDto = new CategoryGoalDetailsDto();

                    // Defensive check for status (assuming GoalId is not null)
                    if (goals.getGoalId().getStatus() != null) {
                        detailsDto.setStatus(goals.getGoalId().getStatus().name());
                    } else {
                        detailsDto.setStatus("UNKNOWN");
                    }

                    // Handle date formatting
                    LocalDate completionDate = goals.getGoalId().getCompletionDate();
                    if (completionDate != null) {
                        detailsDto.setTimeGroup(completionDate.getMonth().minus(1) + " " + completionDate.getYear());
                    } else {
                        detailsDto.setTimeGroup("No Date");
                    }

                    // Set financial amounts
                    // Defensive check to ensure amounts are not null before calculations
                    Double budgetedAmount = goals.getBudgetedAmount() != null ? goals.getBudgetedAmount() : 0.0;
                    Double savedAmount = goals.getSavedAmount() != null ? goals.getSavedAmount() : 0.0;

                    detailsDto.setBudgetedAmount(budgetedAmount);
                    detailsDto.setSavedAmount(savedAmount);

                    if (budgetedAmount > 0) {
                        // Remaining Percentage = ((Budgeted - Saved) / Budgeted) * 100
                        detailsDto.setRemainingPercentage(
                                ((budgetedAmount - savedAmount) / budgetedAmount) * 100.0
                        );
                    } else {
                        // If budget is zero, remaining percentage is 0.0
                        detailsDto.setRemainingPercentage(0.0);
                    }

                    return detailsDto;
                })
                .toList();
    }

    public List<SavingRecordsDto> getAllRecordsByCategory(Long userId, String categoryName) {
        Category category = categoryRepository.findByCategory(categoryName.toUpperCase());
        if (category == null) {
            category = categoryRepository.findByCategory("OTHER");
        }
        return savingRecordsRepository.findAllByGoalId_UserIdAndCategoryGoalId_CategoryId(getUserById(userId), category)
                .stream()
                .map(record -> {
                    SavingRecordsDto savingRecordsDto = new SavingRecordsDto();
                    savingRecordsDto.setTransactionId(record.getTransactionId().getTransactionId());
                    savingRecordsDto.setDate(String.valueOf(record.getDate()));
                    savingRecordsDto.setSavedAmount(record.getSavedAmount());
                    savingRecordsDto.setTimeGroup(record.getTransactionId().getTimeGroup());
                    return savingRecordsDto;
                })
                .toList();
    }

    public List<CategoryGoalIdDto> getListOfCurrentGoalsByCategory(Long userId) {
        Users user = getUserById(userId);
        Month currentMonth = LocalDate.now().getMonth();
        Month nextMonth = LocalDate.now().plusMonths(1).getMonth();

        return goalCategoriesRepository.findAllByGoalId_UserId(user)
                .stream()
                .filter(goal -> {
                    LocalDate completionDate = goal.getGoalId().getCompletionDate();
                    if (completionDate == null) {
                        return false; // Safely ignore goals without a completion date
                    }
                    Month goalMonth = completionDate.getMonth();
                    return goalMonth.equals(currentMonth) || goalMonth.equals(nextMonth);
                })
                .map(goal -> {
                    CategoryGoalIdDto categoryGoalIdDto = new CategoryGoalIdDto();
                    categoryGoalIdDto.setGoalCategoryId(goal.getGoalCategoryId());
                    categoryGoalIdDto.setCategory(goal.getCategoryId().getCategory());
                    return categoryGoalIdDto;
                })
                .toList();
    }

    public Map<String, Double> getOverAllSavingPercentage(Long userId) {
        DecimalFormat df = new DecimalFormat("#.##");
        Users user = getUserById(userId);
        SavingGoals savingGoals = savingGoalsRepository.findByStatusAndUserId(Status.ACTIVE, user);
        Double savingAmount = savingGoals.getGoalAmount();
        Double currentBalance = savingGoals.getCurrentBalance();
        Double percentage = (currentBalance / savingAmount) * 100;
        Map<String, Double> response = new HashMap<>();
        response.put("percentage", percentage.isNaN() ? 0.0 : Double.parseDouble(df.format(percentage)));
        response.put("currentBalance", currentBalance.isNaN() ? 0.0 : Double.parseDouble(df.format(currentBalance)));
        response.put("savingAmount", savingAmount.isNaN() ? 0.0 : Double.parseDouble(df.format(savingAmount)));
        return response;
    }

    public Map<String, Double> getSavingPercentagePerCategory(Long userId, String categoryName) {
        DecimalFormat df = new DecimalFormat("#.##");
        Users user = getUserById(userId);
        Category category = categoryRepository.findByCategory(categoryName.toUpperCase());
        if (category == null) {
            category = categoryRepository.findByCategory("OTHER");
        }
        GoalCategories goalCategories = goalCategoriesRepository.findByCategoryIdAndGoalId_UserIdAndGoalId_Status(category, user, Status.ACTIVE);
        Double savingAmount = goalCategories.getBudgetedAmount();
        Double currentBalance = goalCategories.getSavedAmount();
        Double percentage = (currentBalance / savingAmount) * 100;
        Map<String, Double> response = new HashMap<>();
        response.put("currentBalance", currentBalance.isNaN() ? 0.0 : Double.parseDouble(df.format(currentBalance)));
        response.put("savingAmount", savingAmount.isNaN() ? 0.0 : Double.parseDouble(df.format(savingAmount)));
        response.put("percentage", percentage.isNaN() ? 0.0 : Double.parseDouble(df.format(percentage)));
        return response;
    }

    private Long getCategoryGoalIdFromCategory(String category, Long userId) {
        Category categoryEntity = categoryRepository.findByCategory(category.toUpperCase());
        if (categoryEntity == null) {
            categoryEntity = categoryRepository.findByCategory("OTHER");
        }
        return goalCategoriesRepository.findAllByCategoryIdAndGoalId_UserId(categoryEntity, getUserById(userId))
                .stream()
                .filter(goal -> goal.getGoalId().getCompletionDate().getMonth() == LocalDate.now().getMonth().plus(1)
                        && goal.getGoalId().getCompletionDate().getYear() == LocalDate.now().getYear()
                        && goal.getGoalId().getStatus() == Status.ACTIVE
                )
                .map(GoalCategories::getGoalCategoryId)
                .findFirst()
                .orElse(null);
    }

    // --- Helper Method for Creation ---
    private Double createNewCategoryGoal(Long userId, CategoryGoalsDto categoryGoalsDto) {
        SavingGoals savingGoal = savingGoalsRepository.findByStatusAndUserId(Status.ACTIVE, getUserById(userId));
        GoalCategories goalCategories = new GoalCategories();

        // Set properties
        goalCategories.setGoalId(savingGoal);
        goalCategories.setCategoryId(categoryRepository.findByCategory(categoryGoalsDto.getCategory().toUpperCase()));
        goalCategories.setBudgetedAmount(categoryGoalsDto.getBudgetedAmount());
        goalCategories.setSavedAmount(0.0);
        savingGoal.setGoalAmount(savingGoal.getGoalAmount() + categoryGoalsDto.getBudgetedAmount());

        // Save
        goalCategoriesRepository.save(goalCategories);
        log.info("Category Goal created for user: {}", userId);

        // Update parent Saving Goal amount
        savingGoalsRepository.save(savingGoal);
        log.info("Updated saving goal amount for user: {}", userId);

        pushGoalUpdate(userId);
        return goalCategories.getBudgetedAmount();
    }

    // --- Helper Method for Update ---
    private Double updateExistingCategoryGoal(Long goalCategoryId, Double newBudgetedAmount) throws GoalCategoryNotFoundException {
        GoalCategories goalCategory = goalCategoriesRepository.findByGoalCategoryId(goalCategoryId)
                .orElseThrow(() -> new GoalCategoryNotFoundException("Category goal not found for ID: " + goalCategoryId));

        Double previousBudgetedAmount = goalCategory.getBudgetedAmount();

        // Calculate the difference in amount
        Double amountDifference = newBudgetedAmount - previousBudgetedAmount;

        // Update the Category Goal amount
        goalCategory.setBudgetedAmount(newBudgetedAmount);

        // Update the parent Saving Goal amount
        SavingGoals savingGoal = goalCategory.getGoalId();
        savingGoal.setGoalAmount(savingGoal.getGoalAmount() + amountDifference);

        // Save changes
        goalCategoriesRepository.save(goalCategory);
        log.info("Category Goal updated for Goal ID: {}", goalCategoryId);

        savingGoalsRepository.save(savingGoal);
        log.info("Saving Goal updated for user: {}", savingGoal.getUserId());

        pushGoalUpdate(goalCategory.getGoalId().getUserId().getUserId());
        return goalCategory.getBudgetedAmount();
    }

    private void pushGoalUpdate(Long userId) {
        final String destination = "/topic/goals/" + userId;
        try {
            simpMessagingTemplate.convertAndSend(destination, "Goal Detailed Updated to DB on server");
            log.info("Goals Update broadcast on {}", destination);
        } catch (Exception e) {
            log.error("Failed to broadcast goals on {}. Error: {}", destination, e.getMessage());
        }
    }
}

