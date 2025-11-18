package com.fintech.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
public class Stores {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeId;

    @ManyToOne
    @JoinColumn(name = "chain_id")
    private Chian chainId;

    private String storeName;

    private String locality;

    private String fullAddress;

    @Column(name = "pincode", length = 6)
    private Long pincode;

    public Stores(Chian chainId, String storeName, String locality, String fullAddress, Long pincode){
        this.chainId = chainId;
        this.storeName = storeName;
        this.locality = locality;
        this.fullAddress = fullAddress;
        this.pincode = pincode;
    }
}
