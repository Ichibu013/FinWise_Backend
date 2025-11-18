package com.fintech.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transaction_items")
@Getter
@Setter
public class TransactionItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transactions transactionId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Products productId;

    private Integer quantity;

    private Double pricePerItem;

    private Double totalPrice;
}
