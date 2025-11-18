package com.fintech.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chian")
@Getter
@Setter
@NoArgsConstructor
public class Chian {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long chainId;

    private String chainName;

    public Chian(String chainName) {
        this.chainName = chainName;
    }
}
