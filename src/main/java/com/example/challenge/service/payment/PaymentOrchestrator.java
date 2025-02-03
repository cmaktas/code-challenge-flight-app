package com.example.challenge.service.payment;

import com.example.challenge.domain.enums.PaymentStatus;
import com.example.challenge.service.payment.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOrchestrator {

    private final PaymentRecorderService paymentRecorderService;
    private final PaymentProcessService paymentProcessService;
    private final PaymentValidationService paymentValidationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentReceivedEvent(PaymentReceivedEvent event) {
        log.info("Event: PaymentReceivedEvent => Payment Id={}, Price={}",
                event.getPayment().getId(), event.getPayment().getPrice());
        paymentValidationService.validatePayment(event);
    }

    @EventListener
    public void handlePaymentValidatedEvent(PaymentValidationEvent event) {
        log.info("Event: PaymentValidationEvent => Payment ID={}, Price={}, Status={}",
                event.getPayment().getId(), event.getPayment().getPrice(), event.getPayment().getStatus());
        if (event.getPayment().getStatus() == PaymentStatus.FAILED
                || event.getPayment().getStatus() == PaymentStatus.WAITING) {
            paymentRecorderService.updatePaymentStatus(
                    event.getPayment().getId(),
                    event.getPayment().getStatus()
            );
            return;
        }
        paymentProcessService.callBankService(event);
    }

    @EventListener
    public void handlePaymentProcessCompletedEvent(PaymentProcessEvent event) {
        log.info("Event: PaymentProcessEvent => Payment ID={}, Status={}",
                event.getProceedPayment().getId(), event.getProceedPayment().getStatus());
        paymentRecorderService.updatePaymentStatus(
                event.getProceedPayment().getId(),
                event.getProceedPayment().getStatus());
        paymentValidationService.checkWaitingPayments(event.getProceedPayment());
    }

}
