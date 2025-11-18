package com.fintech.backend.service;

import com.fintech.backend.config.Exceptions.TransactionNotFoundException;
import com.fintech.backend.dto.LastWeekDetailsDto;
import com.fintech.backend.dto.TransactionDetailsDto;
import com.fintech.backend.dto.TransactionItemDto;
import com.fintech.backend.dto.TransactionsDto;
import com.fintech.backend.models.*;
import com.fintech.backend.repository.*;
import com.fintech.backend.utils.enums.Status;
import com.fintech.backend.utils.mappers.GenericDtoMapper;
import com.fintech.backend.utils.mappers.GenericResponseFactory;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.time.format.TextStyle.FULL;
import static java.util.Locale.ENGLISH;

@Slf4j
@Service
public class TransactionsService extends BaseService {

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionItemsRepository transactionItemsRepository;
    private final TransactionsRepository transactionsRepository;
    private final ProductsRepository productsRepository;
    private final GoalCategoriesRepository goalCategoriesRepository;
    private final SavingGoalsRepository savingGoalsRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final SavingRecordsRepository savingRecordsRepository;

    public TransactionsService(GenericDtoMapper mapper,
                               GenericResponseFactory responseFactory,
                               UsersRepository usersRepository,
                               AccountRepository accountRepository,
                               CategoryRepository categoryRepository,
                               TransactionItemsRepository transactionItemsRepository,
                               TransactionsRepository transactionsRepository,
                               ProductsRepository productsRepository,
                               GoalCategoriesRepository goalCategoriesRepository,
                               SavingGoalsRepository savingGoalsRepository,
                               SimpMessagingTemplate simpMessagingTemplate,
                               SavingRecordsRepository savingRecordsRepository) {
        super(mapper, responseFactory, usersRepository);
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionItemsRepository = transactionItemsRepository;
        this.transactionsRepository = transactionsRepository;
        this.productsRepository = productsRepository;
        this.goalCategoriesRepository = goalCategoriesRepository;
        this.savingGoalsRepository = savingGoalsRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.savingRecordsRepository = savingRecordsRepository;
    }

    /**
     * Retrieves detailed information for a specific transaction.
     *
     * @param transactionId the ID of the transaction to retrieve
     * @return a {@link TransactionDetailsDto} populated with transaction core data and its items
     * @throws TransactionNotFoundException if no transaction exists for the given ID
     */
    public TransactionDetailsDto getTransactionDetails(String transactionId) {
        Transactions transaction = transactionsRepository.findByTransactionId(transactionId)
                .orElseThrow(
                        () -> new TransactionNotFoundException("Transaction with ID " + transactionId + " not found.")
                );

        List<TransactionItems> transactionItems = transactionItemsRepository.findAllByTransactionId(transaction);

        List<TransactionItemDto> transactionItemDtos = transactionItems.stream()
                .map(transactionItem -> TransactionItemDto.builder()
                        .totalPrice(transactionItem.getTotalPrice())
                        .productName(transactionItem.getProductId().getProductName())
                        .quantity(transactionItem.getQuantity())
                        .pricePerItem(transactionItem.getPricePerItem())
                        .build())
                .toList();

        TransactionDetailsDto transactionDetailsDto = mapper.map(transaction, TransactionDetailsDto.class);
        transactionDetailsDto.setTransactionId(transaction.getTransactionId());
        transactionDetailsDto.setDate(transaction.getDate().toString());
        transactionDetailsDto.setCategory(transaction.getCategory().getCategory().toUpperCase());
        transactionDetailsDto.setPaymentMethod(transaction.getPaymentMethod());
        transactionDetailsDto.setPaymentAmount(transaction.getTotalTransactionAmount());
        transactionDetailsDto.setTime(transaction.getTime());
        transactionDetailsDto.setTimeGroup(transaction.getTimeGroup());

        transactionDetailsDto.setStatus("COMPLETE");

        transactionDetailsDto.setTransactionItems(transactionItemDtos);

        return transactionDetailsDto;
    }

    /**
     * Retrieves all transactions belonging to a specific user.
     *
     * @param userId the user's ID
     * @return a list of {@link TransactionsDto} mapped from the user's transactions
     */
    public List<TransactionDetailsDto> getAllTransactionsByUserId(Long userId) {
        Users user = getUserById(userId);
        List<String> transactions = transactionsRepository.findAllByUserId(user)
                .stream()
                .map(Transactions::getTransactionId)
                .toList();
        List<TransactionDetailsDto> detailsDtoList = new ArrayList<>();
        for (String transaction : transactions) {
            detailsDtoList.add(getTransactionDetails(transaction));
        }
        return detailsDtoList;
    }

    /**
     * Creates and persists a new transaction for the given user, including its item lines.
     *
     * <p>Also updates the user's account balance and, when applicable, updates the active
     * category saving goals and the parent saving goal amounts.</p>
     *
     * @param userId                the ID of the user performing the transaction
     * @param transactionDetailsDto the transaction payload including items
     * @return a map containing the generated transaction identifier with key {@code "transactionId"}
     */
    @Transactional
    public Map<String, String> createNewTransaction(Long userId, TransactionDetailsDto transactionDetailsDto) {
        Users user = getUserById(userId);
        Accounts account = accountRepository.findByUserId(user);
        Category category = categoryRepository.findByCategory(transactionDetailsDto.getCategory().toUpperCase(ENGLISH));
        GoalCategories goalCategories = goalCategoriesRepository.findByGoalCategoryId(
                getCategoryGoalIdFromCategory(category, user, LocalDate.parse(transactionDetailsDto.getDate())))
                .orElse(null);

        AtomicReference<Double> allItemAmount = new AtomicReference<>(0.0);

        account.setCurrentBalance(
                transactionDetailsDto.getIsExpense()
                        ? account.getCurrentBalance() - transactionDetailsDto.getPaymentAmount()
                        : account.getCurrentBalance() + transactionDetailsDto.getPaymentAmount()
        );
        accountRepository.save(account);

        Transactions transaction = new Transactions();
        transaction.setUserId(user);
        transaction.setTransactionId(
                !transactionDetailsDto.getTransactionId().isEmpty()
                        ? transactionDetailsDto.getTransactionId().trim()
                        : UUID.randomUUID().toString()
        );
        transaction.setTitle(transactionDetailsDto.getTitle());
        transaction.setCategory(
                category == null
                        ? categoryRepository.findByCategory("OTHER")
                        : category
        );
        transaction.setTotalTransactionAmount(transactionDetailsDto.getPaymentAmount());
        transaction.setAccountId(account);
        transaction.setIsExpense(transactionDetailsDto.getIsExpense());
        transaction.setPaymentMethod(transactionDetailsDto.getPaymentMethod());

        LocalDate transactionDate = LocalDate.parse(transactionDetailsDto.getDate());
        transaction.setDate(transactionDate);
        transaction.setTime(transactionDetailsDto.getTime());
        transaction.setTimeGroup(transactionDate.getMonth().getDisplayName(FULL, ENGLISH) + " " + transaction.getDate().getYear());

        transaction.setDescription(transactionDetailsDto.getDescription());

        transactionsRepository.save(transaction);
        log.info("Transaction created successfully");

        // Map Items and Collect to TransactionItems
        List<TransactionItems> items = transactionDetailsDto.getTransactionItems()
                .stream().map((item) -> {
                    TransactionItems transactionItem = new TransactionItems();
                    mapper.map(item, transactionItem);
                    transactionItem.setTransactionId(transaction);
                    transactionItem.setProductId(
                            productsRepository.findByProductName(item.getProductName()) == null
                                    ? productsRepository.save(new Products(item.getProductName(), "pcs"))
                                    : productsRepository.findByProductName(item.getProductName())
                    );
                    transactionItem.setTotalPrice(item.getTotalPrice());
                    allItemAmount.updateAndGet(s -> s + item.getTotalPrice());
                    return transactionItem;
                })
                .toList();
        transactionItemsRepository.saveAll(items);
        log.info("Transaction items created successfully");

        //  Update Category goals and Saving Goals
        if (goalCategories != null) {
            try {
                double savedAmount = allItemAmount.get() - transactionDetailsDto.getPaymentAmount();

                goalCategories.setSavedAmount(goalCategories.getSavedAmount() + savedAmount);
                goalCategories.getGoalId().setCurrentBalance(goalCategories.getGoalId().getCurrentBalance() + savedAmount);
                goalCategoriesRepository.save(goalCategories);
                log.info("Goal Category updated successfully");
                savingGoalsRepository.save(goalCategories.getGoalId());
                log.info("Saving Goal updated successfully");

                SavingRecords savingRecords = new SavingRecords();
                savingRecords.setTransactionId(transaction);
                savingRecords.setCategoryGoalId(goalCategories);
                savingRecords.setGoalId(goalCategories.getGoalId());
                savingRecords.setSavedAmount(savedAmount);
                savingRecords.setDate(transaction.getDate());
                savingRecordsRepository.saveAndFlush(savingRecords);
                log.info("Saving Records created successfully");
            } catch (Exception e) {
                log.error("Failed to update goal category for Goal ID {}. Error: {}", goalCategories.getGoalId().getGoalId(), e.getMessage());
            }
        } else {
            log.info("No Goal Category found for user: {}", user.getUserId());
        }

        pushFinancialSummary(userId);
        pushTransactions(userId);

        return Map.of("transactionId", transaction.getTransactionId());
    }

    /**
     * Calculates the total spending for the given user across all transactions marked as expenses.
     *
     * @param userId the ID of the user
     * @return a map with key {@code "spending"} and the aggregated expense amount as value
     */
    public Map<String, Double> getSpendingPerMonth(Long userId) {
        Users user = getUserById(userId);
        Double Spending = transactionsRepository.findAllByUserId(user)
                .stream()
                .filter((transaction) -> transaction.getIsExpense() == true
                        && transaction.getDate().isAfter(LocalDate.now().withDayOfMonth(1))
                )
                .mapToDouble(Transactions::getTotalTransactionAmount)
                .sum();
        return Map.of("spending", Spending);
    }

    /**
     * Calculates the total income for the given user across all transactions not marked as expenses.
     *
     * @param userId the ID of the user
     * @return a map with key {@code "income"} and the aggregated income amount as value
     */
    public Map<String, Double> getIncomePerMonth(Long userId) {
        Users user = getUserById(userId);
        Double Income = transactionsRepository.findAllByUserId(user)
                .stream()
                .filter((transaction) -> transaction.getIsExpense() == false
                        && transaction.getDate().isAfter(LocalDate.now().withDayOfMonth(1))
                )
                .mapToDouble(Transactions::getTotalTransactionAmount)
                .sum();
        return Map.of("income", Income);
    }

    /**
     * Retrieves the current account balance for the given user.
     *
     * @param userId the user's ID
     * @return a map with key {@code "balance"} and the current balance as value
     */
    public Map<String, Double> getBalance(Long userId) {
        Users user = getUserById(userId);
        Double Balance = accountRepository.findByUserId(user).getCurrentBalance();
        return Map.of("balance", Balance);
    }

    public LastWeekDetailsDto lastWeekDetails(Long userId) {
        LastWeekDetailsDto lastWeekDetailsDto = new LastWeekDetailsDto();
        lastWeekDetailsDto.setIncome(getIncomeLastWeek(userId));
        Map<String, Double> lastWeekSpending = getMostSpendingWithCategoryLastWeek(userId);
        lastWeekDetailsDto.setCategory(lastWeekSpending.keySet().stream().findFirst().orElse("No Category Found"));
        lastWeekDetailsDto.setSpending(lastWeekSpending.values().stream().findFirst().orElse(0.0));
        return lastWeekDetailsDto;
    }

    private double getIncomeLastWeek(Long userId) {
        Users user = getUserById(userId);
        return transactionsRepository.findAllByUserId(user)
                .stream()
                .filter((transaction) -> transaction.getIsExpense() == false
                        && transaction.getDate().isAfter(LocalDate.now().minusWeeks(1)))
                .mapToDouble(Transactions::getTotalTransactionAmount)
                .sum();
    }

    private Map<String, Double> getMostSpendingWithCategoryLastWeek(Long userId) {
        Users user = getUserById(userId);
        List<Transactions> transactions = transactionsRepository.findAllByUserId(user)
                .stream()
                .filter((transaction) -> transaction.getIsExpense() == true
                        && transaction.getDate().isAfter(LocalDate.now().minusWeeks(1)))
                .toList();
        Map<String, Double> totalSpendingPerCategory = transactions.stream()
                .collect(Collectors.groupingBy(
                        transaction -> transaction.getCategory().getCategory(),
                        Collectors.summingDouble(Transactions::getTotalTransactionAmount)
                ));
        Map<String, Double> maxSpending = new HashMap<>();
        totalSpendingPerCategory.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .ifPresent(max -> maxSpending.put(max.getKey(), max.getValue()));
        return maxSpending;
    }

    private void pushFinancialSummary(Long userId) {
        final String destination = "/topic/financial-summary/" + userId;
        try {
            simpMessagingTemplate.convertAndSend(destination, "Financial Summary updated on server");
            log.info("Financial Summary broadcast on {}", destination);
        } catch (Exception e) {
            log.error("Failed to broadcast financial summary on {}. Error: {}", destination, e.getMessage());
        }
    }

    private void pushTransactions(Long userId) {
        final String destination = "/topic/transactions/" + userId;
        try {
            simpMessagingTemplate.convertAndSend(destination, "New transaction added to DB on server");
            log.info("Transactions broadcast on {}", destination);
        } catch (Exception e) {
            log.error("Failed to broadcast transactions on {}. Error: {}", destination, e.getMessage());
        }
    }

    private Long getCategoryGoalIdFromCategory(Category category, Users user, LocalDate date) {
        return goalCategoriesRepository.findAllByCategoryIdAndGoalId_UserId(category, user)
                .stream()
                .filter(goal -> goal.getGoalId().getCompletionDate().getMonth() == date.getMonth().plus(1)
                        && goal.getGoalId().getCompletionDate().getYear() == date.getYear()
                        && goal.getGoalId().getStatus() == Status.ACTIVE
                )
                .map(GoalCategories::getGoalCategoryId)
                .findFirst()
                .orElse(null);
    }
}
