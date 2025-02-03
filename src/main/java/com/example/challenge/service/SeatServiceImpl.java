package com.example.challenge.service;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.infrastructure.exception.BusinessException;
import com.example.challenge.mapper.SeatMapper;
import com.example.challenge.repository.FlightRepository;
import com.example.challenge.repository.SeatRepository;
import com.example.challenge.web.model.v1.request.CreateSeatRequest;
import com.example.challenge.web.model.v1.request.UpdateSeatRequest;
import com.example.challenge.web.model.v1.response.SeatDetailsResponse;
import com.example.challenge.web.model.v1.response.SeatResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final FlightRepository flightRepository;
    private final SeatMapper seatMapper;

    /**
     * Adds a new seat to the specified flight.
     *
     * @param flightId the ID of the flight
     * @param request  the AddSeatRequest with seat details
     * @return SeatResponse representing the newly added seat
     */
    @Override
    public SeatResponse addSeat(Long flightId, CreateSeatRequest request) {
        log.debug("Attempting to add a seat to flight with ID: {}", flightId);

        Flight flight = flightRepository.findByIdWithSeats(flightId)
                .orElseThrow(() -> {
                    log.error("Flight with ID: {} not found", flightId);
                    return new BusinessException("business.error.flight_not_found", HttpStatus.NOT_FOUND);
                });

        Seat seat = seatMapper.mapToSeat(request, flight);
        Seat savedSeat = seatRepository.save(seat);

        int newSeatCapacity = seatRepository.countByFlightId(flightId);
        flight.setSeatCapacity(newSeatCapacity);
        flightRepository.save(flight);

        log.info("Successfully added seat with ID: {} to flight with ID: {}", savedSeat.getId(), flightId);
        return seatMapper.mapToSeatResponse(savedSeat);
    }

    /**
     * Removes a seat by its ID.
     *
     * @param seatId the ID of the seat to be removed
     */
    @Override
    public void removeSeat(Long seatId) {
        log.debug("Attempting to remove seat with ID: {}", seatId);

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> {
                    log.error("Seat with ID: {} not found", seatId);
                    return new BusinessException("business.error.seat_not_found", HttpStatus.NOT_FOUND);
                });

        if (seat.getStatus() == SeatStatus.UNAVAILABLE) {
            log.warn("Cannot remove a sold seat with ID: {}", seatId);
            throw new BusinessException("business.error.sold_seat_cannot_be_removed", HttpStatus.CONFLICT);
        }

        Flight flight = seat.getFlight();
        seatRepository.delete(seat);

        int newSeatCapacity = seatRepository.countByFlightId(flight.getId());
        flight.setSeatCapacity(newSeatCapacity);
        flightRepository.save(flight);

        log.info("Successfully removed seat with ID: {} from flight with ID: {}", seatId, flight.getId());
    }

    /**
     * Updates the details of a seat.
     *
     * @param seatId  the ID of the seat to update
     * @param request the UpdateSeatRequest with updated details
     * @return SeatResponse representing the updated seat
     */
    @Override
    public SeatResponse updateSeat(Long seatId, UpdateSeatRequest request) {
        log.debug("Attempting to update seat with ID: {} using request: {}", seatId, request);

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> {
                    log.error("Seat with ID: {} not found", seatId);
                    return new BusinessException("business.error.seat_not_found", HttpStatus.NOT_FOUND);
                });

        // If the seat is sold (UNAVAILABLE), the price cannot be changed
        if (seat.getStatus() == SeatStatus.UNAVAILABLE && !seat.getPrice().equals(request.getPrice())) {
            log.warn("Cannot change the price of a sold (UNAVAILABLE) seat with ID: {}", seatId);
            throw new BusinessException("business.error.sold_seat_price_cannot_be_updated", HttpStatus.FORBIDDEN);
        }

        seat.setPrice(request.getPrice());
        seat.setStatus(request.getStatus());

        Seat updatedSeat = seatRepository.save(seat);
        log.info("Successfully updated seat with ID: {}", updatedSeat.getId());
        return seatMapper.mapToSeatResponse(updatedSeat);
    }

    /**
     * Fetches the details of a specific seat by its ID.
     *
     * @param seatId the ID of the seat
     * @return SeatDetailsResponse containing the seat and associated flight details
     */
    @Override
    public SeatDetailsResponse getSeatDetails(Long seatId) {
        log.debug("Fetching seat details for seat ID: {}", seatId);

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> {
                    log.error("Seat with ID: {} not found", seatId);
                    return new BusinessException("business.error.seat_not_found", HttpStatus.NOT_FOUND);
                });

        SeatDetailsResponse seatDetails = seatMapper.mapToSeatDetailsResponse(seat);
        log.info("Successfully fetched details for seat ID: {}", seatId);
        return seatDetails;
    }
}