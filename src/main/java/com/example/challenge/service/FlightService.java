package com.example.challenge.service;

import com.example.challenge.web.model.v1.request.CreateFlightRequest;
import com.example.challenge.web.model.v1.response.FlightDetailsResponse;
import com.example.challenge.web.model.v1.request.UpdateFlightRequest;
import com.example.challenge.web.model.v1.response.FlightResponse;

import java.util.List;

public interface FlightService {
    FlightResponse addFlight(CreateFlightRequest request);
    void removeFlight(Long flightId);
    FlightResponse updateFlight(Long flightId, UpdateFlightRequest request);
    List<FlightDetailsResponse> listFlights();
    FlightDetailsResponse getFlightDetails(Long flightId);
}

