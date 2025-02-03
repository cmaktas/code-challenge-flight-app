package com.example.challenge.web.controller.v1;

import com.example.challenge.service.SeatService;
import com.example.challenge.web.model.v1.request.CreateSeatRequest;
import com.example.challenge.web.model.v1.request.UpdateSeatRequest;
import com.example.challenge.web.model.v1.response.SeatDetailsResponse;
import com.example.challenge.web.model.v1.response.SeatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/seats")
@Tag(name = "Seat Controller", description = "APIs for managing seats")
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "Add a new seat to a flight", description = "Adds a new seat to the specified flight. The seat is created with the provided price and status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Seat added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @PostMapping("/{flightId}")
    public ResponseEntity<SeatResponse> addSeat(@PathVariable Long flightId, @Valid @RequestBody CreateSeatRequest request) {
        return new ResponseEntity<>(seatService.addSeat(flightId, request), HttpStatus.CREATED);
    }

    @Operation(summary = "Remove a seat", description = "Removes a seat by its ID. A seat cannot be removed if it has already been sold.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Seat removed successfully"),
            @ApiResponse(responseCode = "404", description = "Seat not found"),
            @ApiResponse(responseCode = "400", description = "Cannot remove a sold seat")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeSeat(@PathVariable Long id) {
        seatService.removeSeat(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a seat", description = "Updates the details of a seat, including its price and status. A seat's status can be updated to mark it as available or unavailable.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seat updated successfully"),
            @ApiResponse(responseCode = "404", description = "Seat not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SeatResponse> updateSeat(@PathVariable Long id, @Valid @RequestBody UpdateSeatRequest request) {
        return ResponseEntity.ok(seatService.updateSeat(id, request));
    }

    @Operation(summary = "Get seat details by ID", description = "Fetches the details of a specific seat by its ID. The response includes seat information along with the associated flight details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seat details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Seat not found")
    })
    @GetMapping("/{id}/details")
    public ResponseEntity<SeatDetailsResponse> getSeatDetails(@PathVariable Long id) {
        SeatDetailsResponse seatDetails = seatService.getSeatDetails(id);
        return ResponseEntity.ok(seatDetails);
    }
}


