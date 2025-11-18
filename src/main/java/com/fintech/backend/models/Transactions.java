package com.fintech.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import static java.time.format.TextStyle.FULL;
import static java.util.Locale.ENGLISH;

@Setter
@Getter
@Entity
@Table(name = "transactions")
public class Transactions {
    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users userId;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Accounts accountId;

    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String timeGroup;

    private LocalDate date;

    private String time;

    private String paymentMethod;

    private Double totalTransactionAmount;

    private Boolean isExpense;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

}