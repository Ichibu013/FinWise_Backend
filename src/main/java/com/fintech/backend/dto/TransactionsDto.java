package com.fintech.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class TransactionsDto {
    private Long transactionId;
    private String title;
    private Double totalTransactionAmount;
    private String date;
    private String Category;
    private Boolean isExpense;
}
