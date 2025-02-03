package com.example.challenge.web.model.v1.request;

import com.example.challenge.domain.enums.SeatStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class UpdateSeatRequest {

    @Schema(description = "Updated price of the seat", example = "199.99")
    @DecimalMin(value = "0.0", inclusive = false, message = "{validation.exception.min}")
    @NotNull(message = "{validation.exception.not_null}")
    @Digits(integer = 8, fraction = 2, message = "{validation.exception.digits}")
    private BigDecimal price;

    @Schema(description = "Updated status of the seat (e.g., AVAILABLE, UNAVAILABLE)", example = "UNAVAILABLE", required = true)
    @NotNull(message = "{validation.exception.not_null}")
    private SeatStatus status;

}
