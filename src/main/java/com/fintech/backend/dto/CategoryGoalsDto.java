package com.fintech.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryGoalsDto {

    private String category;

    private Double budgetedAmount;

    private Double savedAmount;

//    private Long goalCategoryId;

}
