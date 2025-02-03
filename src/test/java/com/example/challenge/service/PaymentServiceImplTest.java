package com.example.challenge.service;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.infrastructure.exception.BusinessException;
import com.example.challenge.repository.FlightRepository;
import com.example.challenge.repository.SeatRepository;
import com.example.challenge.service.payment.PaymentServiceImpl;
import com.example.challenge.web.model.v1.request.PaymentRequest;
import com.example.challenge.web.model.v1.response.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest // Load Spring context with Liquibase migrations applied
@Transactional // Rollback changes after each test
class PaymentServiceImplTest {

    @Autowired
    private PaymentServiceImpl paymentService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Test
    void testConcurrentSeatPurchase() throws InterruptedException {
        // Arrange
        Flight flight = createFlight();
        Seat seat = createSeat(flight, SeatStatus.AVAILABLE, BigDecimal.valueOf(100.00));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successfulPurchases = new AtomicInteger(0);

        Runnable purchaseTask = () -> {
            try {
                PaymentRequest request = new PaymentRequest();
                request.setSeatId(seat.getId());
                request.setPrice(BigDecimal.valueOf(100.00));
                paymentService.purchaseSeat(request);
                successfulPurchases.incrementAndGet();
            } catch (BusinessException e) {
                log.error("Thread [{}] - BusinessException: {}", Thread.currentThread().getName(), e.getMessageKey());
            } finally {
                latch.countDown();
            }
        };

        // Act
        executor.submit(purchaseTask);
        executor.submit(purchaseTask);

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        Seat updatedSeat = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals(SeatStatus.UNAVAILABLE, updatedSeat.getStatus());
        assertEquals(1, successfulPurchases.get(), "Only one thread should successfully purchase the seat.");
    }

    @Test
    void testSeatPurchaseSuccess() {
        Flight flight = createFlight();
        Seat seat = createSeat(flight, SeatStatus.AVAILABLE, BigDecimal.valueOf(100.00));

        PaymentRequest request = new PaymentRequest();
        request.setSeatId(seat.getId());
        request.setPrice(BigDecimal.valueOf(100.00));

        PaymentResponse response = paymentService.purchaseSeat(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Seat purchased successfully.", response.getMessage());

        Seat updatedSeat = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals(SeatStatus.UNAVAILABLE, updatedSeat.getStatus());
    }

    @Test
    void testSeatNotAvailableThrowsException() {
        Flight flight = createFlight();
        Seat seat = createSeat(flight, SeatStatus.UNAVAILABLE, BigDecimal.valueOf(100.00));

        PaymentRequest request = new PaymentRequest();
        request.setSeatId(seat.getId());
        request.setPrice(BigDecimal.valueOf(100.00));

        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.purchaseSeat(request));

        assertEquals("business.error.seat_not_available_for_purchase", exception.getMessageKey());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void testPriceMismatchThrowsException() {
        Flight flight = createFlight();
        Seat seat = createSeat(flight, SeatStatus.AVAILABLE, BigDecimal.valueOf(100.00));

        PaymentRequest request = new PaymentRequest();
        request.setSeatId(seat.getId());
        request.setPrice(BigDecimal.valueOf(200.00));

        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.purchaseSeat(request));

        assertEquals("business.error.seat_price_mismatch", exception.getMessageKey());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testUnexpectedErrorThrowsGenericException() {
        Flight flight = createFlight();
        Seat seat = createSeat(flight, SeatStatus.AVAILABLE, BigDecimal.valueOf(100.00));

        seatRepository.deleteById(seat.getId()); // Simulate seat not found during fetch

        PaymentRequest request = new PaymentRequest();
        request.setSeatId(seat.getId());
        request.setPrice(BigDecimal.valueOf(100.00));

        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.purchaseSeat(request));

        assertEquals("business.error.seat_not_found", exception.getMessageKey());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
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
