package com.fintech.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "goal_categories")
@Getter
@Setter
@RequiredArgsConstructor
public class GoalCategories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalCategoryId;

    @ManyToOne
    @JoinColumn(name = "goal_id")
    private SavingGoals goalId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category categoryId;

    private Double budgetedAmount;

    private Double savedAmount;
}
