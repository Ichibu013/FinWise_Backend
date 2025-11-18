package com.fintech.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class SignupDto {

    private Long userId;

    private String fullName;

    private String email;

    private Long phoneNumber;

    private LocalDate dateOfBirth;

    private String password;
}
