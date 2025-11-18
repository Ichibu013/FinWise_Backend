package com.fintech.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "notifications")
public class Notifications {
    @Id
    private Long id;

    private String timeGroup;

    private String title;

    private String description;

    private String footer;

}
