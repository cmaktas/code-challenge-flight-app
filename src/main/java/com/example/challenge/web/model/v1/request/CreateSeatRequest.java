package com.example.challenge.web.model.v1.request;

import com.example.challenge.domain.enums.SeatStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateSeatRequest {

    @Schema(description = "Price of the seat", example = "199.99", required = true)
    @NotNull(message = "{validation.exception.not_null}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{validation.exception.min}")
    @Digits(integer = 8, fraction = 2, message = "{validation.exception.digits}")
    private BigDecimal price;

    @Schema(description = "Status of the seat (e.g., AVAILABLE, RESERVED, UNAVAILABLE)", example = "AVAILABLE", required = true)
    @NotNull(message = "{validation.exception.not_null}")
    private SeatStatus status;

}
