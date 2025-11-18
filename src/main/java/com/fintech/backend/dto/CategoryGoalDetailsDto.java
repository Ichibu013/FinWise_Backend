package com.fintech.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CategoryGoalDetailsDto {
    private String timeGroup;
    private Double budgetedAmount;
    private Double savedAmount;
    private Double remainingPercentage;
    private String status;
}
