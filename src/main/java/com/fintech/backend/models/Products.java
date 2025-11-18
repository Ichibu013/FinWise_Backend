package com.fintech.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Products {
    // FIX: Map to snake_case 'product_id' and use IDENTITY for typical auto-incrementing PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    // FIX: Map to snake_case 'product_category'
    @Column(name = "product_category")
    private String productCategory;

    // FIX: Map to snake_case 'product_name'
    @Column(name = "product_name")
    private String productName;

    // No fix needed for 'brand' since it is the same in both (unless you prefer explicit mapping)
    private String brand;

    // FIX: Map to snake_case 'standard_unit'
    @Column(name = "standard_unit")
    private String standardUnit;

    // FIX: Map to snake_case 'offer_type'
    @Column(name = "offer_type")
    private String offerType;


    // --- Constructors (No changes needed, but added the final version for completeness) ---

    public Products(String productName, String standardUnit) {
        this.productName = productName;
        this.standardUnit = standardUnit;
    }

    public Products(String productCategory,
                    String productName,
                    String brand,
                    String standardUnit,
                    String offerType) {
        this.productCategory = productCategory;
        this.productName = productName;
        this.brand = brand;
        this.standardUnit = standardUnit;
        this.offerType = offerType;
    }
}