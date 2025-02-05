package com.example.challenge.service.payment;

import com.example.challenge.domain.entity.Payment;
import com.example.challenge.domain.enums.BankResponseCode;
import com.example.challenge.domain.enums.PaymentStatus;
import com.example.challenge.service.BankService;
import com.example.challenge.service.payment.event.PaymentProcessEvent;
import com.example.challenge.service.payment.event.PaymentValidationEvent;
import com.example.challenge.web.model.v1.request.BankPaymentRequest;
import com.example.challenge.web.model.v1.response.BankPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessService {

    private final BankService bankService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Processes the bank call async.
     */
    public void callBankService(PaymentValidationEvent event) {
        PaymentProcessService proxy = (PaymentProcessService) AopContext.currentProxy();
        CompletableFuture.runAsync(() -> proxy.processPaymentWithRetry(event));
    }

    /**
     * Processes the bank response with retry mechanism.
     */
    @Retryable(value = Exception.class, maxAttempts = 2, backoff = @Backoff(delay = 3000))
    public void processPaymentWithRetry(PaymentValidationEvent event) {
        log.info("[Async] Passing payment for Seat Id={} to Bank Service for further processing. Payment Id={}",
                event.getPayment().getSeat().getId(), event.getPayment().getId());

        BankPaymentResponse response = bankService.pay(
                BankPaymentRequest.builder()
                        .price(event.getPayment().getPrice())
                        .build()
        );

        processBankResponse(event.getPayment(), response);
    }

    /**
     * Processes the bank response and updates the payment + seat status.
     */
    private void processBankResponse(Payment payment, BankPaymentResponse response) {
        log.info("Bank call completed for Payment Id={} with Status={}.", payment.getId(), payment.getStatus());
        payment.setStatus(
                BankResponseCode.SUCCESS.getCode().equals(response.getResultCode())
                        ? PaymentStatus.SUCCESS
                        : PaymentStatus.FAILED
        );
        eventPublisher.publishEvent(new PaymentProcessEvent(payment));
    }

    /**
     * Recovery method called when all retry attempts have failed.
     * This method receives the exception and the original PaymentValidationEvent.
     */
    @Recover
    public void recover(Exception e, PaymentValidationEvent event) {
        log.error("[Recover] Payment Id={} failed after retries due to: {}",
                event.getPayment().getId(), e.getMessage());
        event.getPayment().setStatus(PaymentStatus.FAILED);
        eventPublisher.publishEvent(new PaymentProcessEvent(event.getPayment()));
    }
}
