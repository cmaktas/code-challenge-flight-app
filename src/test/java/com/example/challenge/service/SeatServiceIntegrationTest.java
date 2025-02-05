package com.example.challenge.service;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.mapper.SeatMapper;
import com.example.challenge.repository.FlightRepository;
import com.example.challenge.repository.SeatRepository;
import com.example.challenge.web.model.v1.request.CreateSeatRequest;
import com.example.challenge.web.model.v1.response.SeatDetailsResponse;
import com.example.challenge.web.model.v1.response.SeatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class SeatServiceIntegrationTest {

    @Autowired
    private SeatService seatService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private SeatMapper seatMapper;

    @Test
    void addSeat_ShouldPersistSeatInDatabase() {
        // Arrange
        Flight flight = new Flight();
        flight.setFlightNumber("TK123");
        flight.setOrigin("Istanbul");
        flight.setDestination("Berlin");
        flight.setDepartureTime(LocalDateTime.now().plusDays(1));
        flight.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(3));
        flight.setSeatCapacity(1);
        flight.setSeats(new ArrayList<>());
        Flight savedFlight = flightRepository.save(flight);

        CreateSeatRequest request = new CreateSeatRequest();
        request.setPrice(BigDecimal.valueOf(150.00));
        request.setStatus(SeatStatus.AVAILABLE);

        // Act
        SeatResponse response = seatService.addSeat(savedFlight.getId(), request);

        // Assert
        assertNotNull(response);
        assertEquals(1, seatRepository.countByFlightId(savedFlight.getId()));

        Seat savedSeat = seatRepository.findById(response.getSeatId()).orElseThrow();
        assertEquals(request.getPrice(), savedSeat.getPrice());
        assertEquals(request.getStatus(), savedSeat.getStatus());
    }

    @Test
    void removeSeat_ShouldDeleteSeatAndUpdateFlight() {
        // Arrange
        Flight flight = new Flight();
        flight.setFlightNumber("TK123");
        flight.setOrigin("Istanbul");
        flight.setDestination("Berlin");
        flight.setDepartureTime(LocalDateTime.now().plusDays(1));
        flight.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(3));
        flight.setSeatCapacity(1);
        Flight savedFlight = flightRepository.save(flight);

        Seat seat = new Seat();
        seat.setSeatNumber("1A");
        seat.setPrice(BigDecimal.valueOf(150.00));
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setFlight(savedFlight);
        Seat savedSeat = seatRepository.save(seat);

        // Act
        seatService.removeSeat(savedSeat.getId());

        // Assert
        assertFalse(seatRepository.findById(savedSeat.getId()).isPresent());
        Flight updatedFlight = flightRepository.findById(savedFlight.getId()).orElseThrow();
        assertEquals(0, updatedFlight.getSeatCapacity());
    }

    @Test
    void getSeatDetails_ShouldReturnSeatDetails() {
        // Arrange
        Flight flight = new Flight();
        flight.setFlightNumber("TK123");
        flight.setOrigin("Istanbul");
        flight.setDestination("Berlin");
        flight.setSeatCapacity(1);
        flight.setDepartureTime(LocalDateTime.now().plusDays(1));
        flight.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(3));
        Flight savedFlight = flightRepository.save(flight);

        Seat seat = new Seat();
        seat.setSeatNumber("1A");
        seat.setPrice(BigDecimal.valueOf(150.00));
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setFlight(savedFlight);
        Seat savedSeat = seatRepository.save(seat);

        // Act
        SeatDetailsResponse response = seatService.getSeatDetails(savedSeat.getId());

        // Assert
        assertNotNull(response);
        assertEquals(seat.getSeatNumber(), response.getSeat().getSeatNumber());
        assertEquals(savedFlight.getFlightNumber(), response.getFlight().getFlightNumber());
    }
}
