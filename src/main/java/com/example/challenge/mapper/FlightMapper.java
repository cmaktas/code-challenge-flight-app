package com.example.challenge.mapper;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.utils.FlightUtils;
import com.example.challenge.web.model.v1.request.CreateFlightRequest;
import com.example.challenge.web.model.v1.request.UpdateFlightRequest;
import com.example.challenge.web.model.v1.response.FlightDetailsResponse;
import com.example.challenge.web.model.v1.response.FlightResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between Flight entities and their corresponding request and response models.
 * This class centralizes the mapping logic, ensuring consistency across the application.
 */
@Component
public class FlightMapper {

    /**
     * Maps a CreateFlightRequest to a Flight entity.
     *
     * @param request the CreateFlightRequest containing flight creation details
     * @return a Flight entity populated with the request's details
     */
    public Flight mapToFlight(CreateFlightRequest request) {
        return Flight.builder()
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .seatCapacity(request.getSeatCapacity())
                .build();
    }

    /**
     * Maps a Flight entity to a FlightResponse.
     *
     * @param flight the Flight entity to be mapped
     * @return a FlightResponse containing basic flight details
     */
    public FlightResponse mapToFlightResponse(Flight flight) {
        return FlightResponse.builder()
                .flightId(flight.getId()) // Map flightId
                .flightNumber(flight.getFlightNumber())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .seatCapacity(flight.getSeatCapacity())
                .build();
    }

    /**
     * Updates an existing Flight entity with data from an UpdateFlightRequest.
     *
     * @param flight  the existing Flight entity to update
     * @param request the UpdateFlightRequest containing updated flight details
     */
    public void updateFlightFromRequest(Flight flight, UpdateFlightRequest request) {
        flight.setOrigin(request.getOrigin());
        flight.setDestination(request.getDestination());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setFlightNumber(FlightUtils.generateFlightNumber(request.getOrigin(), request.getDestination()));
    }

    /**
     * Maps a Flight entity to a FlightDetailsResponse, including its associated seats.
     *
     * @param flight the Flight entity to be mapped
     * @return a FlightDetailsResponse containing detailed flight information along with seat details
     */
    public FlightDetailsResponse mapToFlightDetailsResponse(Flight flight) {
        List<FlightDetailsResponse.AvailableSeatInfo> availableSeats = mapAvailableSeats(flight);
        List<FlightDetailsResponse.UnavailableSeatInfo> unavailableSeats = mapUnavailableSeats(flight);

        return FlightDetailsResponse.builder()
                .flight(mapToFlightResponse(flight))
                .availableSeats(availableSeats)
                .unavailableSeats(unavailableSeats)
                .build();
    }

    /**
     * Maps available seats of a flight to a list of AvailableSeatInfo.
     *
     * @param flight the Flight entity
     * @return a list of AvailableSeatInfo
     */
    private List<FlightDetailsResponse.AvailableSeatInfo> mapAvailableSeats(Flight flight) {
        return flight.getSeats().stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .map(seat -> FlightDetailsResponse.AvailableSeatInfo.builder()
                        .seatId(seat.getId())
                        .seatNumber(seat.getSeatNumber())
                        .seatPrice(seat.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Maps unavailable seats of a flight to a list of UnavailableSeatInfo.
     *
     * @param flight the Flight entity
     * @return a list of UnavailableSeatInfo
     */
    private List<FlightDetailsResponse.UnavailableSeatInfo> mapUnavailableSeats(Flight flight) {
        return flight.getSeats().stream()
                .filter(seat -> seat.getStatus() == SeatStatus.UNAVAILABLE)
                .map(seat -> FlightDetailsResponse.UnavailableSeatInfo.builder()
                        .seatId(seat.getId())
                        .seatNumber(seat.getSeatNumber())
                        .build())
                .collect(Collectors.toList());
    }
}
