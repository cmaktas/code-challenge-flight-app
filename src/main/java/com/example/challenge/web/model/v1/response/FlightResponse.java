package com.example.challenge.web.model.v1.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightResponse {

    @Schema(description = "ID of the flight", example = "1")
    private Long flightId;

    @Schema(description = "Flight number", example = "TK1234")
    private String flightNumber;

    @Schema(description = "Origin of the flight", example = "Istanbul")
    private String origin;

    @Schema(description = "Destination of the flight", example = "New York")
    private String destination;

    @Schema(description = "Departure time of the flight", example = "2024-12-01T10:00:00")
    private LocalDateTime departureTime;

    @Schema(description = "Arrival time of the flight", example = "2024-12-01T18:00:00")
    private LocalDateTime arrivalTime;

    @Schema(description = "Seat capacity of the flight", example = "150")
    private int seatCapacity;

}
