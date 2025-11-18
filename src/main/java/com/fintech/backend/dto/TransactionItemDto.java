package com.fintech.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class TransactionItemDto {
    private String productName;
    private Double totalPrice;
    private Integer quantity;
    private Double pricePerItem;
}
