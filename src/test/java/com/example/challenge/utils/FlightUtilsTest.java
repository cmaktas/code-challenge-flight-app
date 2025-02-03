package com.example.challenge.utils;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.SeatStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightUtilsTest {

    @Test
    void generateSeats_ShouldGenerateCorrectNumberOfSeats() {
        // Arrange
        Flight flight = new Flight();
        flight.setSeatCapacity(10);

        BigDecimal seatPrice = BigDecimal.valueOf(150.99);

        // Act
        List<Seat> seats = FlightUtils.generateSeats(flight, seatPrice);

        // Assert
        assertEquals(10, seats.size());
        for (int i = 0; i < seats.size(); i++) {
            assertEquals(String.valueOf(i + 1), seats.get(i).getSeatNumber());
            assertEquals(seatPrice, seats.get(i).getPrice());
            assertEquals(SeatStatus.AVAILABLE, seats.get(i).getStatus());
            assertEquals(flight, seats.get(i).getFlight());
        }
    }

    @Test
    void getNextSeatNumber_ShouldReturnNextAvailableSeatNumber() {
        // Arrange
        Flight flight = new Flight();
        Seat seat1 = new Seat();
        seat1.setSeatNumber("1");
        Seat seat2 = new Seat();
        seat2.setSeatNumber("2");
        flight.setSeats(List.of(seat1, seat2));

        // Act
        String nextSeatNumber = FlightUtils.getNextSeatNumber(flight);

        // Assert
        assertEquals("3", nextSeatNumber);
    }

    @Test
    void getNextSeatNumber_ShouldReturnOneWhenNoSeatsExist() {
        // Arrange
        Flight flight = new Flight();
        flight.setSeats(List.of());

        // Act
        String nextSeatNumber = FlightUtils.getNextSeatNumber(flight);

        // Assert
        assertEquals("1", nextSeatNumber);
    }

    @Test
    void generateFlightNumber_ShouldGenerateValidFlightNumber() {
        // Arrange
        String origin = "New York";
        String destination = "London";

        // Act
        String flightNumber = FlightUtils.generateFlightNumber(origin, destination);

        // Assert
        assertTrue(flightNumber.startsWith("NELO"));
        assertEquals(8, flightNumber.length());
        assertDoesNotThrow(() -> Integer.parseInt(flightNumber.substring(4)));
    }

    @Test
    void generateFlightNumber_ShouldHandleSingleCharacterOriginAndDestination() {
        // Arrange
        String origin = "A";
        String destination = "B";

        // Act
        String flightNumber = FlightUtils.generateFlightNumber(origin, destination);

        // Assert
        assertTrue(flightNumber.startsWith("AB"));
        assertEquals(6, flightNumber.length());
        assertDoesNotThrow(() -> Integer.parseInt(flightNumber.substring(2)));
    }
}
