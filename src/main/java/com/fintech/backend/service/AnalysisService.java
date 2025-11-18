package com.fintech.backend.service;

import com.fintech.backend.dto.AnalysisDataDto;
import com.fintech.backend.models.SavingGoals;
import com.fintech.backend.models.Transactions;
import com.fintech.backend.models.Users;
import com.fintech.backend.repository.SavingGoalsRepository;
import com.fintech.backend.repository.TransactionsRepository;
import com.fintech.backend.repository.UsersRepository;
import com.fintech.backend.utils.mappers.GenericDtoMapper;
import com.fintech.backend.utils.mappers.GenericResponseFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AnalysisService extends BaseService {

    private final TransactionsRepository  transactionsRepository;
    private final SavingGoalsRepository  savingGoalsRepository;

    /**
     * Constructs a base service with common utilities shared across services.
     *
     * @param mapper          a generic DTO mapper used for object conversions
     * @param responseFactory a factory for building consistent API responses
     * @param usersRepository repository for accessing {@link Users} entities
     */
    public AnalysisService(GenericDtoMapper mapper,
                           GenericResponseFactory responseFactory,
                           UsersRepository usersRepository,
                           TransactionsRepository transactionsRepository,
                           SavingGoalsRepository savingGoalsRepository) {
        super(mapper, responseFactory, usersRepository);
        this.transactionsRepository = transactionsRepository;
        this.savingGoalsRepository = savingGoalsRepository;
    }

//    public AnalysisDataDto  getAnalysisDataDtoByUserId(Long userId, LocalDate dateAfter, LocalDate dateBefore) {
//        Users user = getUserById(userId);
//        List<Transactions> transactionsList = transactionsRepository.findAllByUserIdAndDateBetween(user, dateAfter, dateBefore);
//        List<SavingGoals> savingGoals = savingGoalsRepository.findAllByUserIdAndCompletionDateBetween(user, dateAfter, dateBefore.plusMonths(1));
//
//    }

}
