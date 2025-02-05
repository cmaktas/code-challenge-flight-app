package com.example.challenge.web.controller.v1;

import com.example.challenge.service.FlightService;
import com.example.challenge.web.model.v1.request.CreateFlightRequest;
import com.example.challenge.web.model.v1.response.FlightDetailsResponse;
import com.example.challenge.web.model.v1.request.UpdateFlightRequest;
import com.example.challenge.web.model.v1.response.FlightResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/flights")
@Tag(name = "Flight Controller", description = "APIs for managing flights")
public class FlightController {

    private final FlightService flightService;

    @Operation(summary = "Add a new flight", description = "Creates a new flight with the given details, including origin, destination, departure time, arrival time, seat capacity, and seat price.")
    @ApiResponse(responseCode = "201", description = "Flight created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @PostMapping
    public ResponseEntity<FlightResponse> addFlight(@Valid @RequestBody CreateFlightRequest request) {
        return new ResponseEntity<>(flightService.addFlight(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Remove a flight", description = "Removes a flight by its ID. The flight cannot be removed if any of its seats are sold.")
    @ApiResponse(responseCode = "204", description = "Flight removed successfully")
    @ApiResponse(responseCode = "404", description = "Flight not found")
    @ApiResponse(responseCode = "400", description = "Flight cannot be removed due to sold seats")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFlight(@PathVariable Long id) {
        flightService.removeFlight(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a flight", description = "Updates the details of an existing flight, such as origin, destination, departure time, arrival time, seat capacity, and seat price. Certain updates may be restricted if seats are sold.")
    @ApiResponse(responseCode = "200", description = "Flight updated successfully")
    @ApiResponse(responseCode = "404", description = "Flight not found")
    @ApiResponse(responseCode = "400", description = "Invalid input or update restricted due to sold seats")
    @PutMapping("/{id}")
    public ResponseEntity<FlightResponse> updateFlight(@PathVariable Long id, @Valid @RequestBody UpdateFlightRequest request) {
        return ResponseEntity.ok(flightService.updateFlight(id, request));
    }

    @Operation(summary = "List all flights", description = "Fetches a paginated list of all future flights, including detailed flight information and associated seat details.")
    @ApiResponse(responseCode = "200", description = "List of flights retrieved successfully")
    @GetMapping
    public ResponseEntity<Page<FlightDetailsResponse>> listFlights(Pageable pageable) {
        return ResponseEntity.ok(flightService.listFlights(pageable));
    }

    @Operation(summary = "Get flight details", description = "Fetches detailed information about a specific flight by its ID, including available and unavailable seats.")
    @ApiResponse(responseCode = "200", description = "Flight details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Flight not found")
    @GetMapping("/{id}/details")
    public ResponseEntity<FlightDetailsResponse> getFlightDetails(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlightDetails(id));
    }
}

