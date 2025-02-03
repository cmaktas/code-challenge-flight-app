package com.example.challenge.web.model.v1.response;

import com.example.challenge.domain.enums.SeatStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {

    @Schema(description = "ID of the seat", example = "101")
    private Long seatId;

    @Schema(description = "Seat number", example = "1A")
    private String seatNumber;

    @Schema(description = "Price of the seat", example = "199.99")
    private BigDecimal price;

    @Schema(description = "Status of the seat (e.g., AVAILABLE, RESERVED, UNAVAILABLE)", example = "AVAILABLE")
    private SeatStatus status;

}
