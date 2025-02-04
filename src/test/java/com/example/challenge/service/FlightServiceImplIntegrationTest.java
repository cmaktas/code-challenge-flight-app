package com.example.challenge.service;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.repository.FlightRepository;
import com.example.challenge.repository.SeatRepository;
import com.example.challenge.utils.FlightUtils;
import com.example.challenge.web.model.v1.request.CreateFlightRequest;
import com.example.challenge.web.model.v1.request.UpdateFlightRequest;
import com.example.challenge.web.model.v1.response.FlightDetailsResponse;
import com.example.challenge.web.model.v1.response.FlightResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.hikari.pool-name=my-db-pool",
        "spring.datasource.hikari.maximum-pool-size=5"
})
@Transactional
class FlightServiceImplIntegrationTest {

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    void addFlight_ShouldSaveFlightAndGenerateSeats() {
        // Arrange
        CreateFlightRequest request = new CreateFlightRequest();
        request.setOrigin("New York");
        request.setDestination("London");
        request.setDepartureTime(LocalDateTime.now().plusDays(1));
        request.setArrivalTime(LocalDateTime.now().plusDays(2));
        request.setSeatCapacity(10);
        request.setSeatPrice(BigDecimal.valueOf(150.00));

        // Act
        FlightResponse response = flightService.addFlight(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getFlightId());
        assertEquals("New York", response.getOrigin());
        assertEquals("London", response.getDestination());

        // Verify seats were created
        Flight savedFlight = flightRepository.findById(response.getFlightId()).orElseThrow();
        List<Seat> seats = savedFlight.getSeats();
        assertEquals(10, seats.size());
        assertEquals(SeatStatus.AVAILABLE, seats.get(0).getStatus());
    }

    @Test
    void removeFlight_ShouldDeleteFlight_WhenNoSeatsAreSold() {
        // Arrange
        Flight flight = new Flight();
        flight.setOrigin("New York");
        flight.setDestination("London");
        flight.setDepartureTime(LocalDateTime.now().plusDays(1));
        flight.setArrivalTime(LocalDateTime.now().plusDays(2));
        flight.setSeatCapacity(10);
        flight.setFlightNumber(FlightUtils.generateFlightNumber(flight.getOrigin(), flight.getDestination()));

        // Initialize seats explicitly
        flight.setSeats(new ArrayList<>());
        flight = flightRepository.saveAndFlush(flight);

        Seat seat = new Seat();
        seat.setSeatNumber("1");
        seat.setPrice(BigDecimal.valueOf(150.00));
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setFlight(flight);

        // Add seat to the initialized list
        flight.getSeats().add(seat);
        flightRepository.saveAndFlush(flight);

        // Act
        flightService.removeFlight(flight.getId());

        // Assert
        assertFalse(flightRepository.findById(flight.getId()).isPresent());
    }


    @Test
    void updateFlight_ShouldUpdateFlightDetails() {
        // Arrange
        Flight flight = new Flight();
        flight.setOrigin("New York");
        flight.setDestination("London");
        flight.setDepartureTime(LocalDateTime.now().plusDays(1));
        flight.setArrivalTime(LocalDateTime.now().plusDays(2));
        flight.setSeatCapacity(10);
        flight.setFlightNumber(FlightUtils.generateFlightNumber(flight.getOrigin(), flight.getDestination()));

        flight = flightRepository.saveAndFlush(flight);

        UpdateFlightRequest request = new UpdateFlightRequest();
        request.setOrigin("Paris");
        request.setDestination("Berlin");
        request.setDepartureTime(LocalDateTime.now().plusDays(3));
        request.setArrivalTime(LocalDateTime.now().plusDays(4));
        request.setSeatCapacity(20);
        request.setSeatPrice(BigDecimal.valueOf(200.00));

        // Act
        FlightResponse response = flightService.updateFlight(flight.getId(), request);

        // Assert
        assertNotNull(response);
        assertEquals("Paris", response.getOrigin());
        assertEquals("Berlin", response.getDestination());

        Flight updatedFlight = flightRepository.findById(flight.getId()).orElseThrow();
        assertEquals(20, updatedFlight.getSeatCapacity());
        assertEquals("Paris", updatedFlight.getOrigin());
        assertEquals("Berlin", updatedFlight.getDestination());
    }

    @Test
    void getFlightDetails_ShouldReturnFlightDetails() {
        // Arrange
        Flight flight = new Flight();
        flight.setOrigin("New York");
        flight.setDestination("London");
        flight.setDepartureTime(LocalDateTime.now().plusDays(1));
        flight.setArrivalTime(LocalDateTime.now().plusDays(2));
        flight.setSeatCapacity(10);
        flight.setFlightNumber(FlightUtils.generateFlightNumber(flight.getOrigin(), flight.getDestination()));
        flight.setSeats(new ArrayList<>()); // Explicit initialization

        flight = flightRepository.saveAndFlush(flight);

        Seat seat = new Seat();
        seat.setSeatNumber("1");
        seat.setPrice(BigDecimal.valueOf(150.00));
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setFlight(flight);

        // Add seat to the initialized list
        flight.getSeats().add(seat);
        flightRepository.saveAndFlush(flight);

        // Act
        FlightDetailsResponse response = flightService.getFlightDetails(flight.getId());

        // Assert
        assertNotNull(response);
        assertEquals("New York", response.getFlight().getOrigin());
        assertEquals("London", response.getFlight().getDestination());
        assertEquals(1, response.getAvailableSeats().size());
        assertEquals("1", response.getAvailableSeats().get(0).getSeatNumber());
    }


    @Test
    void listFlights_ShouldReturnFutureFlights() {
        // Arrange
        Flight flight1 = new Flight();
        flight1.setOrigin("New York");
        flight1.setDestination("London");
        flight1.setDepartureTime(LocalDateTime.now().plusDays(1));
        flight1.setArrivalTime(LocalDateTime.now().plusDays(2));
        flight1.setSeatCapacity(10);
        flight1.setFlightNumber(FlightUtils.generateFlightNumber(flight1.getOrigin(), flight1.getDestination()));
        flight1.setSeats(new ArrayList<>()); // Explicit initialization

        Flight flight2 = new Flight();
        flight2.setOrigin("Paris");
        flight2.setDestination("Berlin");
        flight2.setDepartureTime(LocalDateTime.now().plusDays(3));
        flight2.setArrivalTime(LocalDateTime.now().plusDays(4));
        flight2.setSeatCapacity(20);
        flight2.setFlightNumber(FlightUtils.generateFlightNumber(flight2.getOrigin(), flight2.getDestination()));
        flight2.setSeats(new ArrayList<>()); // Explicit initialization

        flightRepository.saveAllAndFlush(List.of(flight1, flight2));

        // Act
        List<FlightDetailsResponse> flights = flightService.listFlights();

        // Assert
        assertEquals(2, flights.size());
        assertEquals("New York", flights.get(0).getFlight().getOrigin());
        assertEquals("Paris", flights.get(1).getFlight().getOrigin());
    }


}
