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

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

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
    @Transactional(propagation = REQUIRES_NEW)
    public Payment createPendingPayment(Seat seat, BigDecimal price) {
        Payment pendingPayment = paymentRepository.save(Payment.builder()
                .createdAt(LocalDateTime.now())
                .seat(seat)
                .price(price)
                .status(PaymentStatus.PENDING)
                .build());
        log.info("Created PENDING Payment Id={} for Seat Id={}", pendingPayment.getId(), seat.getId());
        eventPublisher.publishEvent(new PaymentReceivedEvent(pendingPayment));
        return pendingPayment;
    }

    /**
     * Update the payment status and seat availability.
     */
    @Transactional(propagation = REQUIRES_NEW)
    public void updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment not found: " + paymentId));
        payment.setStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Updated Payment Id={} to Status={}", updatedPayment.getId(), updatedPayment.getStatus());
        if (status == PaymentStatus.SUCCESS) {
            log.info("Payment Id={} Bank Call is:{}. Updating Seat Id={} to UNAVAILABLE",
                    updatedPayment.getId(),
                    updatedPayment.getStatus(),
                    updatedPayment.getSeat().getId());
            Seat seat = payment.getSeat();
            seat.setStatus(SeatStatus.UNAVAILABLE);
            seatRepository.save(seat);
        }
    }
}
