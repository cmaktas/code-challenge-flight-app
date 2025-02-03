package com.example.challenge.mapper;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.web.model.v1.request.CreateFlightRequest;
import com.example.challenge.web.model.v1.response.FlightDetailsResponse;
import com.example.challenge.web.model.v1.response.FlightResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightMapperTest {

    private FlightMapper flightMapper;

    @BeforeEach
    void setUp() {
        flightMapper = new FlightMapper();
    }

    @Test
    void testMapToFlight() {
        CreateFlightRequest request = new CreateFlightRequest();
        request.setOrigin("New York");
        request.setDestination("London");
        request.setDepartureTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        request.setArrivalTime(LocalDateTime.of(2024, 1, 1, 18, 0));
        request.setSeatCapacity(150);

        Flight flight = flightMapper.mapToFlight(request);

        assertEquals("New York", flight.getOrigin());
        assertEquals("London", flight.getDestination());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), flight.getDepartureTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 18, 0), flight.getArrivalTime());
        assertEquals(150, flight.getSeatCapacity());
    }

    @Test
    void testMapToFlightResponse() {
        Flight flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("NYL001");
        flight.setOrigin("New York");
        flight.setDestination("London");
        flight.setDepartureTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        flight.setArrivalTime(LocalDateTime.of(2024, 1, 1, 18, 0));
        flight.setSeatCapacity(150);

        FlightResponse response = flightMapper.mapToFlightResponse(flight);

        assertEquals(1L, response.getFlightId());
        assertEquals("NYL001", response.getFlightNumber());
        assertEquals("New York", response.getOrigin());
        assertEquals("London", response.getDestination());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), response.getDepartureTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 18, 0), response.getArrivalTime());
        assertEquals(150, response.getSeatCapacity());
    }

    @Test
    void testMapToFlightDetailsResponse() {
        Flight flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("NYL001");
        flight.setOrigin("New York");
        flight.setDestination("London");

        Seat availableSeat = new Seat();
        availableSeat.setId(1L);
        availableSeat.setSeatNumber("1A");
        availableSeat.setPrice(BigDecimal.valueOf(500.0));
        availableSeat.setStatus(SeatStatus.AVAILABLE);

        Seat unavailableSeat = new Seat();
        unavailableSeat.setId(2L);
        unavailableSeat.setSeatNumber("1B");
        unavailableSeat.setStatus(SeatStatus.UNAVAILABLE);

        flight.setSeats(List.of(availableSeat, unavailableSeat));

        FlightDetailsResponse response = flightMapper.mapToFlightDetailsResponse(flight);

        assertEquals(1, response.getAvailableSeats().size());
        assertEquals("1A", response.getAvailableSeats().get(0).getSeatNumber());

        assertEquals(1, response.getUnavailableSeats().size());
        assertEquals("1B", response.getUnavailableSeats().get(0).getSeatNumber());
    }
}
