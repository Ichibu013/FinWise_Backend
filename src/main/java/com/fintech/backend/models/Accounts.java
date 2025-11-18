package com.fintech.backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestBody;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@RequiredArgsConstructor
public class Accounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @OneToOne
    @JoinColumn(name = "user_id")
    private Users userId;

    @Column(name = "account_name")
    private Double currentBalance = 0.0;

}
