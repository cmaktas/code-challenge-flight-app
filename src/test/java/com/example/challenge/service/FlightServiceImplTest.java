package com.example.challenge.service;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.infrastructure.exception.BusinessException;
import com.example.challenge.mapper.FlightMapper;
import com.example.challenge.repository.FlightDao;
import com.example.challenge.repository.SeatRepository;
import com.example.challenge.web.model.v1.request.CreateFlightRequest;
import com.example.challenge.web.model.v1.request.UpdateFlightRequest;
import com.example.challenge.web.model.v1.response.FlightDetailsResponse;
import com.example.challenge.web.model.v1.response.FlightResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class FlightServiceImplTest {

    @InjectMocks
    private FlightServiceImpl flightService;

    @Mock
    private FlightDao flightDao;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private FlightMapper flightMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addFlight_ShouldAddNewFlight() {
        // Arrange
        CreateFlightRequest request = new CreateFlightRequest();
        request.setOrigin("New York");
        request.setDestination("London");
        request.setDepartureTime(LocalDateTime.now().plusDays(1));
        request.setArrivalTime(LocalDateTime.now().plusDays(2));
        request.setSeatCapacity(100);
        request.setSeatPrice(BigDecimal.valueOf(200.00));

        Flight flight = new Flight();
        flight.setId(1L);

        when(flightMapper.mapToFlight(request)).thenReturn(flight);
        when(flightDao.saveFlight(any(Flight.class))).thenReturn(flight);
        when(flightMapper.mapToFlightResponse(flight)).thenReturn(new FlightResponse());

        // Act
        FlightResponse response = flightService.addFlight(request);

        // Assert
        assertNotNull(response);
        verify(flightMapper).mapToFlight(request);
        verify(flightDao).saveFlight(any(Flight.class));
    }

    @Test
    void removeFlight_ShouldRemoveFlight() {
        // Arrange
        Long flightId = 1L;
        Flight flight = new Flight();
        Seat seat = new Seat();
        seat.setStatus(SeatStatus.AVAILABLE);
        flight.setSeats(List.of(seat));

        when(flightDao.getFlightById(flightId)).thenReturn(flight);

        // Act
        flightService.removeFlight(flightId);

        // Assert
        verify(flightDao).deleteFlightById(flightId);
    }

    @Test
    void removeFlight_ShouldThrowException_WhenSeatsAreSold() {
        // Arrange
        Long flightId = 1L;
        Flight flight = new Flight();
        Seat seat = new Seat();
        seat.setStatus(SeatStatus.UNAVAILABLE);
        flight.setSeats(List.of(seat));

        when(flightDao.getFlightById(flightId)).thenReturn(flight);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> flightService.removeFlight(flightId));
        assertEquals("business.error.flight_has_sold_seats", exception.getMessageKey());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void updateFlight_ShouldUpdateFlight() {
        // Arrange
        Long flightId = 1L;
        UpdateFlightRequest request = new UpdateFlightRequest();
        request.setOrigin("New York");
        request.setDestination("London");
        request.setDepartureTime(LocalDateTime.now().plusDays(1));
        request.setArrivalTime(LocalDateTime.now().plusDays(2));
        request.setSeatCapacity(100);
        request.setSeatPrice(BigDecimal.valueOf(300.00));

        Flight flight = new Flight();
        // Create a flight with an available seat and a different seat capacity
        Seat seat = new Seat();
        seat.setStatus(SeatStatus.AVAILABLE);
        flight.setSeats(List.of(seat));
        flight.setSeatCapacity(50);

        when(flightDao.getFlightById(flightId)).thenReturn(flight);
        doNothing().when(seatRepository).deleteAll(anyList());
        when(flightDao.saveFlight(flight)).thenReturn(flight);
        when(flightMapper.mapToFlightResponse(flight)).thenReturn(new FlightResponse());

        // Act
        FlightResponse response = flightService.updateFlight(flightId, request);

        // Assert
        assertNotNull(response);
        verify(flightDao).saveFlight(flight);
        verify(seatRepository).deleteAll(anyList());
    }

    @Test
    void listFlights_ShouldReturnPaginatedFutureFlights() {
        // Arrange
        Flight flight = new Flight();
        flight.setDepartureTime(LocalDateTime.now().plusDays(1));
        FlightDetailsResponse details = new FlightDetailsResponse();
        when(flightDao.getAllFlightsWithSeats()).thenReturn(List.of(flight));
        when(flightMapper.mapToFlightDetailsResponse(flight)).thenReturn(details);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<FlightDetailsResponse> page = flightService.listFlights(pageable);

        // Assert
        assertEquals(1, page.getTotalElements());
        verify(flightDao).getAllFlightsWithSeats();
    }

    @Test
    void getFlightDetails_ShouldReturnFlightDetails() {
        // Arrange
        Long flightId = 1L;
        Flight flight = new Flight();

        when(flightDao.getFlightById(flightId)).thenReturn(flight);
        when(flightMapper.mapToFlightDetailsResponse(flight)).thenReturn(new FlightDetailsResponse());

        // Act
        FlightDetailsResponse response = flightService.getFlightDetails(flightId);

        // Assert
        assertNotNull(response);
        verify(flightDao).getFlightById(flightId);
    }

    @Test
    void getFlightDetails_ShouldThrowException_WhenFlightNotFound() {
        // Arrange
        Long flightId = 1L;

        when(flightDao.getFlightById(flightId))
                .thenThrow(new BusinessException("business.error.flight_not_found", HttpStatus.NOT_FOUND));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> flightService.getFlightDetails(flightId));
        assertEquals("business.error.flight_not_found", exception.getMessageKey());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
