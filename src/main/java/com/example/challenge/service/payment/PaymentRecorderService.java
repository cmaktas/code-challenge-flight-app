package com.example.challenge.service.payment;

import com.example.challenge.domain.entity.Payment;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.PaymentStatus;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.repository.PaymentRepository;
import com.example.challenge.repository.SeatRepository;
import com.example.challenge.service.payment.event.PaymentReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRecorderService {

    private final PaymentRepository paymentRepository;
    private final SeatRepository seatRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Creates a PENDING payment and publishes an event.
     */
    @Transactional
    public Payment createPendingPayment(Seat seat, BigDecimal price) {
        Payment pendingPayment = paymentRepository.save(Payment.builder()
                .createdAt(LocalDateTime.now())
                .seat(seat)
                .price(price)
                .status(PaymentStatus.PENDING)
                .build());
        log.info("[Payment Recorder Service] Created PENDING Payment Id={} for Seat Id={}", pendingPayment.getId(), seat.getId());
        eventPublisher.publishEvent(new PaymentReceivedEvent(pendingPayment));
        return pendingPayment;
    }

    /**
     * Update the payment status and seat availability.
     */
    @Transactional
    public void updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment not found: " + paymentId));
        payment.setStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("[Payment Recorder Service] Updated Payment Id={} to Status={}", updatedPayment.getId(), updatedPayment.getStatus());
        if (status == PaymentStatus.SUCCESS) {
            log.info("[Payment Recorder Service] Payment Id={} Bank Call is:{}. Updating Seat Id={} to UNAVAILABLE",
                    updatedPayment.getId(),
                    updatedPayment.getStatus(),
                    updatedPayment.getSeat().getId());
            Seat seat = payment.getSeat();
            seat.setStatus(SeatStatus.UNAVAILABLE);
            seatRepository.save(seat);
        }
    }
}
