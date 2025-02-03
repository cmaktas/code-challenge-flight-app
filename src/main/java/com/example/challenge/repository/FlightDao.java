package com.example.challenge.repository;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.infrastructure.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Helper class for FlightRepository, encapsulating database operations
 * and transactional boundaries.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlightDao {

    private final FlightRepository flightRepository;

    /**
     * Retrieves a flight with its seats by ID.
     * Handles transactional boundaries and custom exception mapping.
     *
     * @param flightId the ID of the flight
     * @return the flight entity
     * @throws BusinessException if the flight is not found
     */
    @Transactional(readOnly = true)
    public Flight getFlightById(Long flightId) {
        return flightRepository.findByIdWithSeats(flightId)
                .orElseThrow(() -> {
                    log.error("Flight with ID: {} not found", flightId);
                    return new BusinessException("business.error.flight_not_found", HttpStatus.NOT_FOUND);
                });
    }

    /**
     * Retrieves all flights with their associated seats.
     * @return list of flights
     */
    @Transactional(readOnly = true)
    public List<Flight> getAllFlightsWithSeats() {
        return flightRepository.findAllWithSeats();
    }

    /**
     * Deletes a flight by its ID.
     *
     * @param flightId the ID of the flight to delete
     */
    @Transactional
    public void deleteFlightById(Long flightId) {
        if (!flightRepository.existsById(flightId)) {
            log.warn("Flight with ID {} does not exist. Skipping delete operation.", flightId);
            throw new BusinessException("business.error.flight_not_found", HttpStatus.NOT_FOUND);
        }
        flightRepository.deleteById(flightId);
        log.info("Flight with ID {} successfully deleted.", flightId);
    }

    /**
     * Saves a flight.
     *
     * @param flight Flight to be saved
     */
    @Transactional
    public Flight saveFlight(Flight flight) {
        return flightRepository.save(flight);
    }
}
