package com.example.challenge.service;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.infrastructure.exception.BusinessException;
import com.example.challenge.repository.FlightDao;
import com.example.challenge.repository.SeatRepository;
import com.example.challenge.utils.FlightUtils;
import com.example.challenge.mapper.FlightMapper;
import com.example.challenge.web.model.v1.request.CreateFlightRequest;
import com.example.challenge.web.model.v1.response.FlightDetailsResponse;
import com.example.challenge.web.model.v1.request.UpdateFlightRequest;
import com.example.challenge.web.model.v1.response.FlightResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing flights.
 * Provides functionality to add, update, remove, list flights and fetch flight details
 */
@Slf4j
@AllArgsConstructor
@Service
public class FlightServiceImpl implements FlightService {

    private final FlightDao flightDao;
    private final SeatRepository seatRepository;
    private final FlightMapper flightMapper;

    /**
     * Adds a new flight based on the provided request.
     *
     * @param request the request containing flight details
     * @return the created flight response
     */
    @Override
    public FlightResponse addFlight(CreateFlightRequest request) {
        log.debug("Starting to add a new flight with request: {}", request);
        Flight flight = flightMapper.mapToFlight(request);
        flight.setFlightNumber(FlightUtils.generateFlightNumber(request.getOrigin(), request.getDestination()));
        flight.setSeats(FlightUtils.generateSeats(flight, request.getSeatPrice()));
        Flight savedFlight = flightDao.saveFlight(flight);
        log.info("Successfully added a new flight with ID: {}", savedFlight.getId());
        return flightMapper.mapToFlightResponse(savedFlight);
    }

    /**
     * Removes a flight by its ID. Throws an exception if the flight is not found
     * or if some seats are already sold.
     *
     * @param flightId the ID of the flight to remove
     */
    @Override
    public void removeFlight(Long flightId) {
        log.debug("Attempting to remove flight with ID: {}", flightId);
        Flight flight = flightDao.getFlightById(flightId);
        boolean hasSoldSeats = flight.getSeats().stream()
                .anyMatch(seat -> seat.getStatus() == SeatStatus.UNAVAILABLE);
        if (hasSoldSeats) {
            log.warn("Cannot remove flight with ID: {} because some seats have been sold", flightId);
            throw new BusinessException("business.error.flight_has_sold_seats", HttpStatus.CONFLICT);
        }
        flightDao.deleteFlightById(flightId);
        log.info("Successfully removed flight with ID: {}", flightId);
    }

    /**
     * Updates a flight by its ID using the provided request.
     * Handles seat price updates, capacity changes, and ensures no conflicts with sold seats.
     *
     * @param flightId the ID of the flight to update
     * @param request the request containing updated flight details
     * @return the updated flight response
     */
    @Override
    public FlightResponse updateFlight(Long flightId, UpdateFlightRequest request) {
        log.debug("Attempting to update flight with ID: {} using request: {}", flightId, request);
        Flight flight = flightDao.getFlightById(flightId);

        // Generate seats if none exist
        if (CollectionUtils.isEmpty(flight.getSeats())) {
            log.debug("No seats found for flight ID: {}. Generating new seats.", flightId);
            flight.setSeats(FlightUtils.generateSeats(flight, request.getSeatPrice()));
        }

        // Prevent seat capacity update if sold seats exist
        boolean hasSoldSeats = flight.getSeats().stream()
                .anyMatch(seat -> seat.getStatus() == SeatStatus.UNAVAILABLE);
        if (hasSoldSeats && request.getSeatCapacity() != flight.getSeatCapacity()) {
            log.warn("Cannot change the seat capacity for flight ID: {} because some seats have been sold", flightId);
            throw new BusinessException("business.error.flight_capacity_cannot_be_updated_due_to_sold_seats", HttpStatus.CONFLICT);
        }

        // Update seat prices if applicable
        if (!request.getSeatPrice().equals(flight.getSeats().get(0).getPrice())) {
            log.debug("Updating seat prices for flight ID: {}", flightId);
            flight.getSeats().stream()
                    .filter(seat -> seat.getStatus() != SeatStatus.UNAVAILABLE)
                    .forEach(seat -> seat.setPrice(request.getSeatPrice()));
        }

        // Update seat capacity if applicable
        if (request.getSeatCapacity() != flight.getSeatCapacity()) {
            log.debug("Updating seat capacity for flight ID: {}", flightId);
            flight.setSeatCapacity(request.getSeatCapacity());
            List<Seat> newSeats = FlightUtils.generateSeats(flight, request.getSeatPrice());
            seatRepository.deleteAll(flight.getSeats());
            flight.setSeats(newSeats);
        }

        flightMapper.updateFlightFromRequest(flight, request);
        Flight updatedFlight = flightDao.saveFlight(flight);
        log.info("Successfully updated flight with ID: {}", updatedFlight.getId());
        return flightMapper.mapToFlightResponse(updatedFlight);
    }

    /**
     * Fetches a list of all future flights.
     *
     * @return a list of flight details responses
     */
    @Override
    public List<FlightDetailsResponse> listFlights() {
        log.debug("Fetching list of future flights");
        LocalDateTime now = LocalDateTime.now();
        List<FlightDetailsResponse> flights = flightDao.getAllFlightsWithSeats().stream()
                .filter(flight -> flight.getDepartureTime().isAfter(now))
                .map(flightMapper::mapToFlightDetailsResponse)
                .collect(Collectors.toList());
        log.info("Fetched {} future flights", flights.size());
        return flights;
    }

    /**
     * Fetches detailed information for a specific flight by its ID.
     *
     * @param flightId the ID of the flight
     * @return the flight details response
     */
    @Override
    public FlightDetailsResponse getFlightDetails(Long flightId) {
        log.debug("Fetching flight details for flight ID: {}", flightId);
        Flight flight = flightDao.getFlightById(flightId);
        FlightDetailsResponse response = flightMapper.mapToFlightDetailsResponse(flight);
        log.info("Successfully fetched details for flight ID: {}", flightId);
        return response;
    }

}
