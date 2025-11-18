package com.fintech.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "saving_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavingRecords {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long recordId;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private Transactions transactionId;

    @ManyToOne
    @JoinColumn(name = "goal_id")
    private SavingGoals goalId;

    @ManyToOne
    @JoinColumn(name = "category_goal_id")
    private GoalCategories categoryGoalId;

    private Double savedAmount;

    private LocalDate date;
}
