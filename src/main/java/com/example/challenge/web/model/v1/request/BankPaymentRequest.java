package com.example.challenge.web.model.v1.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
public class BankPaymentRequest {

    @Schema(description = "Price of the payment", example = "100.00", required = true)
    @NotNull(message = "{validation.exception.not_null}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{validation.exception.min}")
    @Digits(integer = 8, fraction = 2, message = "{validation.exception.digits}")
    private BigDecimal price;

}
