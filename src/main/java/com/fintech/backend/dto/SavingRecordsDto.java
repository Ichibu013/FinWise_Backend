package com.fintech.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingRecordsDto {
    private String transactionId;
    private Double savedAmount;
    private String date;
    private String timeGroup;
}
