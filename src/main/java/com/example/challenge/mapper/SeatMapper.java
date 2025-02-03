package com.example.challenge.mapper;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.utils.FlightUtils;
import com.example.challenge.web.model.v1.request.CreateSeatRequest;
import com.example.challenge.web.model.v1.response.SeatDetailsResponse;
import com.example.challenge.web.model.v1.response.SeatResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between Seat entities and their corresponding request and response models.
 * This class centralizes the mapping logic for Seat-related operations, ensuring consistency across the application.
 */
@Component
public class SeatMapper {

    /**
     * Maps an AddSeatRequest to a Seat entity.
     *
     * @param request the AddSeatRequest containing seat creation details
     * @param flight  the Flight entity to which the seat belongs
     * @return a Seat entity populated with the request's details
     */
    public Seat mapToSeat(CreateSeatRequest request, Flight flight) {
        return Seat.builder()
                .seatNumber(FlightUtils.getNextSeatNumber(flight)) // Generates the next seat number
                .price(request.getPrice())
                .status(request.getStatus())
                .flight(flight)
                .build();
    }

    /**
     * Maps a Seat entity to a SeatResponse.
     * This response contains basic information about the seat, such as its ID, number, price, and status.
     *
     * @param seat the Seat entity to be mapped
     * @return a SeatResponse containing basic seat details
     */
    public SeatResponse mapToSeatResponse(Seat seat) {
        return SeatResponse.builder()
                .seatId(seat.getId()) // Map seatId
                .seatNumber(seat.getSeatNumber())
                .price(seat.getPrice())
                .status(seat.getStatus())
                .build();
    }

    /**
     * Maps a Seat entity to a SeatDetailsResponse, embedding basic seat details and flight information.
     * Uses SeatResponse for seat details and builds FlightInfo for the associated flight.
     *
     * @param seat the Seat entity to be mapped
     * @return a SeatDetailsResponse containing detailed seat and flight information
     */
    public SeatDetailsResponse mapToSeatDetailsResponse(Seat seat) {
        Flight flight = seat.getFlight();
        return SeatDetailsResponse.builder()
                .seat(mapToSeatResponse(seat)) // Reuses SeatResponse for basic seat details
                .flight(SeatDetailsResponse.FlightInfo.builder()
                        .flightNumber(flight.getFlightNumber())
                        .origin(flight.getOrigin())
                        .destination(flight.getDestination())
                        .departureTime(flight.getDepartureTime())
                        .arrivalTime(flight.getArrivalTime())
                        .build())
                .build();
    }
}
