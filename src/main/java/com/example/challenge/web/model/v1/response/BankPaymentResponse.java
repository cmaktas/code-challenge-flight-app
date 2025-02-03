package com.example.challenge.web.model.v1.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankPaymentResponse {

    @Schema(description = "Result code of the payment operation", example = "200")
    private String resultCode;

}
