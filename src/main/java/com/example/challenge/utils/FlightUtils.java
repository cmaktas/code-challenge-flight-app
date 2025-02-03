package com.example.challenge.utils;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.SeatStatus;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for operations related to flights and seats.
 * Provides methods for generating seats, creating unique seat numbers,
 * and generating unique flight numbers based on flight details.
 * This class is stateless and should only contain static utility methods.
 */
@UtilityClass
public class FlightUtils {

    /**
     * Generates a list of seats for a given flight based on its seat capacity and seat price.
     * Each seat is assigned a sequential seat number.
     *
     * @param flight    the flight for which seats are generated
     * @param seatPrice the price for each seat
     * @return a list of generated Seat objects
     */
    public static List<Seat> generateSeats(Flight flight, BigDecimal seatPrice) {
        return IntStream.rangeClosed(1, flight.getSeatCapacity())
                .mapToObj(seatNumber -> createSeat(String.valueOf(seatNumber), seatPrice, flight))
                .collect(Collectors.toList());
    }

    /**
     * Creates a single seat for a flight with the given seat number and price.
     * The seat is initialized with the AVAILABLE status.
     *
     * @param seatName  the seat number as a string
     * @param seatPrice the price for the seat
     * @param flight    the flight to which the seat belongs
     * @return a Seat object
     */
    private static Seat createSeat(String seatName, BigDecimal seatPrice, Flight flight) {
        return Seat.builder()
                .seatNumber(seatName)
                .price(seatPrice)
                .status(SeatStatus.AVAILABLE)
                .flight(flight)
                .build();
    }

    /**
     * Finds the next available seat number for a flight by scanning existing seat numbers.
     * This ensures that seat numbers are unique within the flight.
     *
     * @param flight the flight for which to determine the next available seat number
     * @return the next available seat number as a string
     */
    public static String getNextSeatNumber(Flight flight) {
        List<String> existingSeatNumbers = flight.getSeats().stream()
                .map(Seat::getSeatNumber)
                .collect(Collectors.toList());
        int seatNumber = 1;
        while (existingSeatNumbers.contains(String.valueOf(seatNumber))) {
            seatNumber++;
        }
        return String.valueOf(seatNumber);
    }

    /**
     * Generates a unique flight number based on the flight's origin and destination.
     * The flight number is composed of the first two characters of the origin and destination
     * (converted to uppercase) followed by a random 4-digit number.
     *
     * @param origin      the origin location of the flight
     * @param destination the destination location of the flight
     * @return a unique flight number
     */
    public static String generateFlightNumber(String origin, String destination) {
        String originCode = origin.length() > 1 ? origin.substring(0, 2).toUpperCase() : origin.toUpperCase();
        String destinationCode = destination.length() > 1 ? destination.substring(0, 2).toUpperCase() : destination.toUpperCase();
        StringBuilder flightNumber = new StringBuilder();
        return flightNumber.append(originCode)
                .append(destinationCode)
                .append((int) (Math.random() * 9000) + 1000).toString();
    }
}