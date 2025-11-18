package com.fintech.backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryGoalIdDto {
    private Long goalCategoryId;
    private String category;

}
