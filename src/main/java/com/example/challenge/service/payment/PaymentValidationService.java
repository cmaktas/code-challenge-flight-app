package com.example.challenge.service.payment;

import com.example.challenge.domain.entity.Payment;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.PaymentStatus;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.repository.PaymentRepository;
import com.example.challenge.repository.SeatRepository;
import com.example.challenge.service.payment.event.PaymentReceivedEvent;
import com.example.challenge.service.payment.event.PaymentValidationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles validation of newly created payments and coordinates
 * "waiting" payments after a previous payment succeeds or fails.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentValidationService {

    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Validates payment. Checks seat availability, ensures no
     * successful payment exists, and checks if there's an older pending/waiting payment.
     *
     * @param event PaymentCreationEvent containing the new Payment
     */
    @Transactional
    public void validatePayment(PaymentReceivedEvent event) {
        Payment payment = event.getPayment();
        Seat seat = seatRepository.findById(payment.getSeat().getId())
                .orElseThrow(() -> new IllegalStateException("Seat not found"));

        if (isSeatUnavailable(seat, payment)) {
            return;
        }
        if (hasSuccessfulPayment(seat, payment)) {
            return;
        }
        if (hasOlderPendingPayment(seat, payment)) {
            return;
        }

        log.info("Payment ID={} is validated => publish PaymentValidatedEvent", payment.getId());
        eventPublisher.publishEvent(new PaymentValidationEvent(payment));
    }

    /**
     * Invoked after a payment completes (SUCCESS or FAILED). If it is SUCCESS,
     * we fail all WAITING payments for that seat. If it is FAILED, we promote
     * the next oldest WAITING payment (by changing it to PENDING and re-validating).
     *
     * @param completedPayment the Payment that just finished bank processing
     */
    @Transactional
    public void checkWaitingPayments(Payment completedPayment) {
        log.info("Payment Id={} for Seat Id={} Status is={}. {}.",
                completedPayment.getId(),
                completedPayment.getSeat().getId(),
                completedPayment.getStatus(),
                completedPayment.getStatus() == PaymentStatus.SUCCESS ? "Failing other payments for the seat" : "Finding next waiting payment for the seat");
        if (completedPayment.getStatus() == PaymentStatus.SUCCESS) {
            failWaitingPayments(completedPayment.getSeat().getId());
        } else if (completedPayment.getStatus() == PaymentStatus.FAILED) {
            Payment nextPayment = findNextWaitingPayment(completedPayment.getSeat().getId());
            if (nextPayment != null) {
                log.info("Next WAITING payment found => Id={}, Publishing PaymentValidationEvent...", nextPayment.getId());
                nextPayment.setStatus(PaymentStatus.PENDING);
                eventPublisher.publishEvent(new PaymentValidationEvent(nextPayment));
            }
        }
    }

    /**
     * Checks if the seat is already unavailable. If so, marks payment as FAILED
     * and publishes a PaymentValidationEvent.
     *
     * @return true if the seat is unavailable and we have marked the payment as FAILED
     */
    private boolean isSeatUnavailable(Seat seat, Payment payment) {
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            log.info("Seat Id={} is already UNAVAILABLE => Payment Id={} fails immediately",
                    payment.getSeat().getId(),
                    payment.getId());
            payment.setStatus(PaymentStatus.FAILED);
            eventPublisher.publishEvent(new PaymentValidationEvent(payment));
            return true;
        }
        return false;
    }

    /**
     * Checks if there's already a SUCCESS payment for the seat.
     * If found, marks the new payment as FAILED and publishes PaymentValidationEvent.
     *
     * @return true if a successful payment is found
     */
    private boolean hasSuccessfulPayment(Seat seat, Payment payment) {
        if (paymentRepository.existsBySeatIdAndStatus(seat.getId(), PaymentStatus.SUCCESS)) {
            log.info("Another SUCCESS payment found for Seat Id={}, Payment Id={} fails",
                    payment.getSeat().getId(),
                    payment.getId());
            payment.setStatus(PaymentStatus.FAILED);
            eventPublisher.publishEvent(new PaymentValidationEvent(payment));
            return true;
        }
        return false;
    }

    /**
     * Checks if there is an older PENDING/WAITING payment. If so, marks this new payment
     * as WAITING and publishes PaymentValidationEvent.
     *
     * @return true if there is an older concurrent payment
     */
    private boolean hasOlderPendingPayment(Seat seat, Payment payment) {
        if (paymentRepository.existsBySeatIdAndOlderPending(
                seat.getId(),
                payment.getCreatedAt())) {
            log.info("Older PENDING/WAITING payment exists for Seat Id={}. Mark Payment Id={} as WAITING",
                    payment.getSeat().getId(),
                    payment.getId());
            payment.setStatus(PaymentStatus.WAITING);
            eventPublisher.publishEvent(new PaymentValidationEvent(payment));
            return true;
        }
        return false;
    }

    /**
     * Fails all WAITING payments for the given seat ID.
     */
    private void failWaitingPayments(Long seatId) {
        List<Payment> waitingPayments = paymentRepository.findAllBySeatIdAndStatusIn(seatId, List.of(PaymentStatus.PENDING, PaymentStatus.WAITING));
        for (Payment w : waitingPayments) {
            w.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(w);
            log.info("Payment Id={} Status is moved from WAITING => FAILED because seat is sold", w.getId());
        }
    }

    /**
     * Finds the earliest WAITING payment (by creation time) for the seat
     * and returns it, or null if none found.
     */
    private Payment findNextWaitingPayment(Long seatId) {
        return paymentRepository.findFirstBySeatIdAndStatusOrderByCreatedAtAsc(seatId, PaymentStatus.WAITING);
    }
}
