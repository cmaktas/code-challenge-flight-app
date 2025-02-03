package com.example.challenge.web.model.v1.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @Schema(description = "ID of the seat to purchase", example = "1", required = true)
    @NotNull(message = "{validation.exception.not_null}")
    private Long seatId;

    @Schema(description = "Price of the seat", example = "100.00", required = true)
    @NotNull(message = "{validation.exception.not_null}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{validation.exception.min}")
    @Digits(integer = 8, fraction = 2, message = "{validation.exception.digits}")
    private BigDecimal price;

}
