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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeatServiceImplTest {

    @InjectMocks
    private SeatServiceImpl seatService;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private SeatMapper seatMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addSeat_ShouldAddNewSeat() {
        // Arrange
        Long flightId = 1L;
        CreateSeatRequest request = new CreateSeatRequest();
        request.setPrice(BigDecimal.valueOf(200.00));
        request.setStatus(SeatStatus.AVAILABLE);

        Flight flight = new Flight();
        flight.setId(flightId);

        Seat seat = new Seat();
        seat.setId(1L);

        when(flightRepository.findByIdWithSeats(flightId)).thenReturn(Optional.of(flight));
        when(seatMapper.mapToSeat(request, flight)).thenReturn(seat);
        when(seatRepository.save(any(Seat.class))).thenReturn(seat);
        when(seatRepository.countByFlightId(flightId)).thenReturn(10);
        when(seatMapper.mapToSeatResponse(seat)).thenReturn(new SeatResponse());

        // Act
        SeatResponse response = seatService.addSeat(flightId, request);

        // Assert
        assertNotNull(response);
        verify(flightRepository).findByIdWithSeats(flightId);
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    void removeSeat_ShouldRemoveSeat() {
        // Arrange
        Long seatId = 1L;
        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setStatus(SeatStatus.AVAILABLE);

        Flight flight = new Flight();
        flight.setId(1L);
        seat.setFlight(flight);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(seatRepository.countByFlightId(flight.getId())).thenReturn(9);

        // Act
        seatService.removeSeat(seatId);

        // Assert
        verify(seatRepository).delete(seat);
        verify(flightRepository).save(any(Flight.class));
    }

    @Test
    void removeSeat_ShouldThrowException_WhenSeatIsSold() {
        // Arrange
        Long seatId = 1L;
        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setStatus(SeatStatus.UNAVAILABLE);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> seatService.removeSeat(seatId));
        assertEquals("business.error.sold_seat_cannot_be_removed", exception.getMessageKey());
        verify(seatRepository, never()).delete(any(Seat.class));
    }

    @Test
    void updateSeat_ShouldUpdateSeat() {
        // Arrange
        Long seatId = 1L;
        UpdateSeatRequest request = new UpdateSeatRequest();
        request.setPrice(BigDecimal.valueOf(250.00));
        request.setStatus(SeatStatus.UNAVAILABLE);

        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setPrice(BigDecimal.valueOf(200.00));
        seat.setStatus(SeatStatus.AVAILABLE);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(seatRepository.save(seat)).thenReturn(seat);
        when(seatMapper.mapToSeatResponse(seat)).thenReturn(new SeatResponse());

        // Act
        SeatResponse response = seatService.updateSeat(seatId, request);

        // Assert
        assertNotNull(response);
        assertEquals(request.getPrice(), seat.getPrice());
        assertEquals(request.getStatus(), seat.getStatus());
        verify(seatRepository).save(seat);
    }

    @Test
    void updateSeat_ShouldThrowException_WhenPriceOfSoldSeatIsUpdated() {
        // Arrange
        Long seatId = 1L;
        UpdateSeatRequest request = new UpdateSeatRequest();
        request.setPrice(BigDecimal.valueOf(250.00));
        request.setStatus(SeatStatus.UNAVAILABLE);

        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setPrice(BigDecimal.valueOf(200.00));
        seat.setStatus(SeatStatus.UNAVAILABLE);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> seatService.updateSeat(seatId, request));
        assertEquals("business.error.sold_seat_price_cannot_be_updated", exception.getMessageKey());
        verify(seatRepository, never()).save(any(Seat.class));
    }

    @Test
    void getSeatDetails_ShouldReturnSeatDetails() {
        // Arrange
        Long seatId = 1L;
        Seat seat = new Seat();
        seat.setId(seatId);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(seatMapper.mapToSeatDetailsResponse(seat)).thenReturn(new SeatDetailsResponse());

        // Act
        SeatDetailsResponse response = seatService.getSeatDetails(seatId);

        // Assert
        assertNotNull(response);
        verify(seatRepository).findById(seatId);
    }

    @Test
    void getSeatDetails_ShouldThrowException_WhenSeatNotFound() {
        // Arrange
        Long seatId = 1L;

        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> seatService.getSeatDetails(seatId));
        assertEquals("business.error.seat_not_found", exception.getMessageKey());
        verify(seatRepository).findById(seatId);
    }
}
