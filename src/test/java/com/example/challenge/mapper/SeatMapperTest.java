package com.example.challenge.mapper;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.web.model.v1.request.CreateSeatRequest;
import com.example.challenge.web.model.v1.response.SeatDetailsResponse;
import com.example.challenge.web.model.v1.response.SeatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SeatMapperTest {

    private SeatMapper seatMapper;

    @BeforeEach
    void setUp() {
        seatMapper = new SeatMapper();
    }

    @Test
    void testMapToSeat() {
        // Arrange
        Flight flight = Flight.builder()
                .id(1L)
                .flightNumber("TK123")
                .origin("Istanbul")
                .destination("Ankara")
                .departureTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .arrivalTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .build();

        CreateSeatRequest request = new CreateSeatRequest();
        request.setPrice(BigDecimal.valueOf(200.00));
        request.setStatus(SeatStatus.AVAILABLE);

        // Act
        Seat seat = seatMapper.mapToSeat(request, flight);

        // Assert
        assertEquals("1", seat.getSeatNumber()); // Assuming this is the first seat in the flight
        assertEquals(BigDecimal.valueOf(200.00), seat.getPrice());
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertEquals(flight, seat.getFlight());
    }

    @Test
    void testMapToSeatResponse() {
        // Arrange
        Seat seat = Seat.builder()
                .id(1L)
                .seatNumber("1A")
                .price(BigDecimal.valueOf(150.00))
                .status(SeatStatus.AVAILABLE)
                .build();

        // Act
        SeatResponse response = seatMapper.mapToSeatResponse(seat);

        // Assert
        assertEquals(1L, response.getSeatId());
        assertEquals("1A", response.getSeatNumber());
        assertEquals(BigDecimal.valueOf(150.00), response.getPrice());
        assertEquals(SeatStatus.AVAILABLE, response.getStatus());
    }

    @Test
    void testMapToSeatDetailsResponse() {
        // Arrange
        Flight flight = Flight.builder()
                .id(1L)
                .flightNumber("TK123")
                .origin("Istanbul")
                .destination("Ankara")
                .departureTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .arrivalTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .build();

        Seat seat = Seat.builder()
                .id(1L)
                .seatNumber("1A")
                .price(BigDecimal.valueOf(150.00))
                .status(SeatStatus.AVAILABLE)
                .flight(flight)
                .build();

        // Act
        SeatDetailsResponse response = seatMapper.mapToSeatDetailsResponse(seat);

        // Assert
        assertNotNull(response.getSeat());
        assertEquals(1L, response.getSeat().getSeatId());
        assertEquals("1A", response.getSeat().getSeatNumber());
        assertEquals(BigDecimal.valueOf(150.00), response.getSeat().getPrice());
        assertEquals(SeatStatus.AVAILABLE, response.getSeat().getStatus());

        assertNotNull(response.getFlight());
        assertEquals("TK123", response.getFlight().getFlightNumber());
        assertEquals("Istanbul", response.getFlight().getOrigin());
        assertEquals("Ankara", response.getFlight().getDestination());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), response.getFlight().getDepartureTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), response.getFlight().getArrivalTime());
    }
}
