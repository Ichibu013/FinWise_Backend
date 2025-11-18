package com.fintech.backend.models;

import com.fintech.backend.utils.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "saving_goals")
@Getter
@Setter
public class SavingGoals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users userId;

    private String goalName;

    private Double goalAmount;

    private Double currentBalance;

    private LocalDate completionDate;

    @Enumerated(EnumType.STRING)
    private Status status;

}

