package com.example.challenge.service;

import com.example.challenge.web.model.v1.request.CreateSeatRequest;
import com.example.challenge.web.model.v1.request.UpdateSeatRequest;
import com.example.challenge.web.model.v1.response.SeatDetailsResponse;
import com.example.challenge.web.model.v1.response.SeatResponse;

public interface SeatService {
    SeatResponse addSeat(Long flightId, CreateSeatRequest request);
    void removeSeat(Long seatId);
    SeatResponse updateSeat(Long seatId, UpdateSeatRequest request);
    SeatDetailsResponse getSeatDetails(Long id);
}
