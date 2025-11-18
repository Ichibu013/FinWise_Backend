package com.fintech.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "product_prices")
@Getter
@Setter
@NoArgsConstructor
public class ProductPrices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long priceRecordId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Products productId;

    @ManyToOne
    @JoinColumn(name = "chain_id")
    private Chian chainId;

    private Double price;

    private LocalDate dateRecorded;

    public ProductPrices(Products productId, Chian chainId, Double price, LocalDate dateRecorded) {
        this.productId = productId;
        this.chainId = chainId;
        this.price = price;
        this.dateRecorded = dateRecorded;
    }
}
