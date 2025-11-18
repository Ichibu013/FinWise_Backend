package com.fintech.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class TransactionDetailsDto {
    private String title;
    private String timeGroup;
    private String category;
    private String description;
    private String transactionId;
    private String date;
    private String time;
    private String status;
    private String paymentMethod;
    private Double paymentAmount;
    private List<TransactionItemDto> transactionItems;
    private Boolean isExpense;
}

