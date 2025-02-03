package com.example.challenge.domain.enums;

import lombok.Getter;

@Getter
public enum BankResponseCode {
    SUCCESS("200"),
    FAILED("400");

    private final String code;

    BankResponseCode(String code) {
        this.code = code;
    }
}
