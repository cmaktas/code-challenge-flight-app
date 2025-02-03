package com.example.challenge.web.model.v1.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateFlightRequest {

    @Schema(description = "Updated origin of the flight", example = "New York", required = true)
    @NotBlank(message = "{validation.exception.not_blank}")
    private String origin;

    @Schema(description = "Updated destination of the flight", example = "London", required = true)
    @NotBlank(message = "{validation.exception.not_blank}")
    private String destination;

    @Schema(description = "Updated departure time of the flight", example = "2024-12-01T10:00:00", required = true)
    @Future(message = "{validation.exception.future}")
    private LocalDateTime departureTime;

    @Schema(description = "Updated arrival time of the flight", example = "2024-12-01T18:00:00", required = true)
    @Future(message = "{validation.exception.future}")
    private LocalDateTime arrivalTime;

    @Schema(description = "Updated seat capacity of the flight", example = "150", required = true)
    @Min(value = 100, message = "{validation.exception.min}")
    private int seatCapacity;

    @Schema(description = "Updated price of each seat", example = "299.99", required = true)
    @NotNull(message = "{validation.exception.not_null}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{validation.exception.min}")
    @Digits(integer = 8, fraction = 2, message = "{validation.exception.digits}")
    private BigDecimal seatPrice;

}
