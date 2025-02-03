package com.example.challenge.web.model.v1.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightDetailsResponse {

    @Schema(description = "Details of the flight")
    private FlightResponse flight;

    @Schema(description = "List of available seats")
    private List<AvailableSeatInfo> availableSeats;

    @Schema(description = "List of unavailable seats")
    private List<UnavailableSeatInfo> unavailableSeats;

    @Data
    @Builder
    public static class AvailableSeatInfo {

        @Schema(description = "ID of the seat", example = "101")
        private Long seatId;

        @Schema(description = "Seat number", example = "1A")
        private String seatNumber;

        @Schema(description = "Price of the seat", example = "199.99")
        private BigDecimal seatPrice;
    }

    @Data
    @Builder
    public static class UnavailableSeatInfo {

        @Schema(description = "ID of the seat", example = "102")
        private Long seatId;

        @Schema(description = "Seat number", example = "2B")
        private String seatNumber;
    }

}
