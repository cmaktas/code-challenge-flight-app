package com.example.challenge.service.payment;

import com.example.challenge.domain.entity.Payment;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.PaymentStatus;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.infrastructure.exception.BusinessException;
import com.example.challenge.repository.PaymentRepository;
import com.example.challenge.repository.SeatRepository;
import com.example.challenge.web.model.v1.request.PaymentRequest;
import com.example.challenge.web.model.v1.response.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentServiceImplUnitTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentRecorderService paymentRecorderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set a dummy web request so that ServletUriComponentsBuilder works.
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @Test
    void purchaseSeat_ShouldReturnPendingResponse_WhenValid() {
        // Arrange
        Long seatId = 1L;
        BigDecimal price = BigDecimal.valueOf(100.00);
        PaymentRequest request = PaymentRequest.builder()
                .seatId(seatId)
                .price(price)
                .build();

        Seat seat = Seat.builder()
                .id(seatId)
                .price(price)
                .status(SeatStatus.AVAILABLE)
                .build();

        Payment pendingPayment = Payment.builder()
                .id(10L)
                .createdAt(java.time.LocalDateTime.now())
                .seat(seat)
                .price(price)
                .status(PaymentStatus.PENDING)
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(paymentRepository.existsBySeatIdAndStatus(seatId, PaymentStatus.SUCCESS)).thenReturn(false);
        when(paymentRecorderService.createPendingPayment(eq(seat), eq(price))).thenReturn(pendingPayment);

        // Act
        PaymentResponse response = paymentService.purchaseSeat(request);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentStatus.PENDING.toString(), response.getStatus());
        assertEquals(10L, response.getPaymentId());
        verify(paymentRecorderService, times(1)).createPendingPayment(seat, price);
    }

    @Test
    void purchaseSeat_ShouldThrowException_WhenSeatNotFound() {
        // Arrange
        Long seatId = 1L;
        PaymentRequest request = PaymentRequest.builder()
                .seatId(seatId)
                .price(BigDecimal.valueOf(100.00))
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.purchaseSeat(request));
        assertEquals("business.error.seat_not_found", ex.getMessageKey());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void purchaseSeat_ShouldThrowException_WhenSeatNotAvailable() {
        // Arrange
        Long seatId = 1L;
        PaymentRequest request = PaymentRequest.builder()
                .seatId(seatId)
                .price(BigDecimal.valueOf(100.00))
                .build();

        Seat seat = Seat.builder()
                .id(seatId)
                .price(BigDecimal.valueOf(100.00))
                .status(SeatStatus.UNAVAILABLE)
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.purchaseSeat(request));
        assertEquals("business.error.seat_not_available_for_purchase", ex.getMessageKey());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void purchaseSeat_ShouldThrowException_WhenSuccessfulPaymentExists() {
        // Arrange
        Long seatId = 1L;
        PaymentRequest request = PaymentRequest.builder()
                .seatId(seatId)
                .price(BigDecimal.valueOf(100.00))
                .build();

        Seat seat = Seat.builder()
                .id(seatId)
                .price(BigDecimal.valueOf(100.00))
                .status(SeatStatus.AVAILABLE)
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(paymentRepository.existsBySeatIdAndStatus(seatId, PaymentStatus.SUCCESS)).thenReturn(true);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.purchaseSeat(request));
        assertEquals("business.error.seat_not_available_for_purchase", ex.getMessageKey());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void purchaseSeat_ShouldThrowException_WhenPriceMismatch() {
        // Arrange
        Long seatId = 1L;
        PaymentRequest request = PaymentRequest.builder()
                .seatId(seatId)
                .price(BigDecimal.valueOf(90.00)) // mismatching price
                .build();

        Seat seat = Seat.builder()
                .id(seatId)
                .price(BigDecimal.valueOf(100.00))
                .status(SeatStatus.AVAILABLE)
                .build();

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(paymentRepository.existsBySeatIdAndStatus(seatId, PaymentStatus.SUCCESS)).thenReturn(false);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.purchaseSeat(request));
        assertEquals("business.error.seat_price_mismatch", ex.getMessageKey());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
