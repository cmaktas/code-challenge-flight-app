package com.example.challenge.service;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.repository.FlightRepository;
import com.example.challenge.repository.SeatRepository;
import com.example.challenge.service.payment.PaymentServiceImpl;
import com.example.challenge.web.model.v1.request.PaymentRequest;
import com.example.challenge.web.model.v1.response.PaymentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class PaymentServiceImplIntegrationTest {

    @Autowired
    private PaymentServiceImpl paymentService;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    void testSeatPurchasePersistsCorrectly() {
        // Arrange
        Flight flight = createFlight();
        Seat seat = createSeat(flight, SeatStatus.AVAILABLE, BigDecimal.valueOf(150.00));

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setSeatId(seat.getId());
        paymentRequest.setPrice(BigDecimal.valueOf(150.00));

        // Act
        PaymentResponse paymentResponse = paymentService.purchaseSeat(paymentRequest);

        // Assert
        assertNotNull(paymentResponse, "The payment response should not be null.");
        assertEquals("SUCCESS", paymentResponse.getStatus(), "The payment status should be SUCCESS.");
        assertEquals("Seat purchased successfully.", paymentResponse.getMessage(), "The message should confirm success.");

        Seat updatedSeat = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals(SeatStatus.UNAVAILABLE, updatedSeat.getStatus(), "The seat status should be UNAVAILABLE after purchase.");
    }

    @Test
    void testIntegrationWithIyzicoPaymentService() {
        // Arrange
        Flight flight = createFlight();
        Seat seat = createSeat(flight, SeatStatus.AVAILABLE, BigDecimal.valueOf(100.00));

        PaymentRequest request = new PaymentRequest();
        request.setSeatId(seat.getId());
        request.setPrice(BigDecimal.valueOf(100.00));

        // Act
        PaymentResponse response = paymentService.purchaseSeat(request);

        // Assert
        assertEquals("SUCCESS", response.getStatus(), "The payment status should be SUCCESS.");
        Seat updatedSeat = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals(SeatStatus.UNAVAILABLE, updatedSeat.getStatus(), "The seat status should be UNAVAILABLE.");
    }

    // Helper method for concurrent purchase testing
    private void executePurchase(Long seatId, BigDecimal price) {
        try {
            PaymentRequest request = new PaymentRequest();
            request.setSeatId(seatId);
            request.setPrice(price);
            paymentService.purchaseSeat(request);
        } catch (Exception e) {
            // Ignore exceptions for this test case
        }
    }

    // Helper methods to create flight and seat
    private Flight createFlight() {
        Flight flight = Flight.builder()
                .flightNumber("TK123")
                .origin("Istanbul")
                .destination("Ankara")
                .departureTime(LocalDateTime.now().plusHours(2))
                .arrivalTime(LocalDateTime.now().plusHours(4))
                .seatCapacity(150)
                .seats(new ArrayList<>())
                .build();
        return flightRepository.saveAndFlush(flight);
    }

    private Seat createSeat(Flight flight, SeatStatus status, BigDecimal price) {
        Seat seat = Seat.builder()
                .seatNumber("1A")
                .price(price)
                .status(status)
                .flight(flight)
                .build();
        return seatRepository.saveAndFlush(seat);
    }
}
