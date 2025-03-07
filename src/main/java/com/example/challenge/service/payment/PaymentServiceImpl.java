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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Handles seat purchases and payment status retrieval.
 */
@Slf4j
@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentRecorderService paymentRecorderService;

    /**
     * Processes seat purchase request.
     */
    @Override
    @Transactional
    public PaymentResponse purchaseSeat(PaymentRequest paymentRequest) {
        Seat seat = validateSeat(paymentRequest.getSeatId(), paymentRequest.getPrice());
        Payment payment = paymentRecorderService.createPendingPayment(seat, paymentRequest.getPrice());
        log.info("Seat Id={} purchase initiated. Payment Id={} is PENDING. Bank call will happen async.",
                paymentRequest.getSeatId(), payment.getId());
        return PaymentResponse.builder()
                .status(payment.getStatus().toString())
                .message("Seat purchase is being processed asynchronously.")
                .paymentId(payment.getId())
                .statusCheckUrl(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/v1/payments/{paymentId}")
                        .buildAndExpand(payment.getId())
                        .toUriString())
                .build();
    }

    /**
     * Retrieves payment status by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(payment -> PaymentResponse.builder()
                        .status(payment.getStatus().name())
                        .message(getPaymentMessage(payment.getStatus()))
                        .paymentId(payment.getId())
                        .statusCheckUrl(ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/api/v1/payments/{paymentId}")
                                .buildAndExpand(paymentId)
                                .toUriString())
                        .build())
                .orElseThrow(() -> new BusinessException("business.error.payment_not_found", HttpStatus.NOT_FOUND));
    }

    /**
     * Returns a message based on payment status.
     */
    private String getPaymentMessage(PaymentStatus status) {
        switch (status) {
            case SUCCESS:
                return "Seat has been purchased successfully.";
            case FAILED:
                return "Purchase failed.";
            default:
                return "Current status: " + status.name();
        }
    }

    /**
     * Validates seat availability, checks pending payments, and verifies price.
     */
    private Seat validateSeat(Long seatId, BigDecimal requestedPrice) {

        Seat seat = seatRepository.findById(seatId).orElseThrow(
                () -> new BusinessException("business.error.seat_not_found", HttpStatus.NOT_FOUND)
        );

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new BusinessException("business.error.seat_not_available_for_purchase", HttpStatus.CONFLICT);
        }
        if (paymentRepository.existsBySeatIdAndStatus(seatId, PaymentStatus.SUCCESS)) {
            throw new BusinessException("business.error.seat_not_available_for_purchase", HttpStatus.CONFLICT);
        }
        if (seat.getPrice().compareTo(requestedPrice) != 0) {
            throw new BusinessException("business.error.seat_price_mismatch", HttpStatus.BAD_REQUEST);
        }

        return seat;
    }

}
