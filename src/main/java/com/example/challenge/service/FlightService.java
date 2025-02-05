package com.example.challenge.service;

import com.example.challenge.web.model.v1.request.CreateFlightRequest;
import com.example.challenge.web.model.v1.response.FlightDetailsResponse;
import com.example.challenge.web.model.v1.request.UpdateFlightRequest;
import com.example.challenge.web.model.v1.response.FlightResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface FlightService {
    FlightResponse addFlight(CreateFlightRequest request);
    void removeFlight(Long flightId);
    FlightResponse updateFlight(Long flightId, UpdateFlightRequest request);
    Page<FlightDetailsResponse> listFlights(Pageable pageable);
    FlightDetailsResponse getFlightDetails(Long flightId);
}

