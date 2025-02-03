package com.example.challenge.service;

import com.example.challenge.domain.enums.BankResponseCode;
import com.example.challenge.web.model.v1.request.BankPaymentRequest;
import com.example.challenge.web.model.v1.response.BankPaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class BankService {

    private final Random random = new Random();

    /**
     * Simulates a bank payment with random response codes, variable latency, and occasional timeouts.
     */
    public BankPaymentResponse pay(BankPaymentRequest request) {
        try {
            // Simulate a random delay between 2 to 7 seconds
            Thread.sleep(getRandomDelay());

            // Simulate a rare timeout exception
            if (random.nextInt(100) < 10) {
                log.error("Timeout occurred while processing payment.");
                throw new RuntimeException("Bank Service Timeout");
            }

            // Simulate bank response with 30% failure rate
            boolean isSuccessful = random.nextInt(100) >= 30; // 70% success, 30% failure
            String responseCode = isSuccessful ? BankResponseCode.SUCCESS.getCode() : BankResponseCode.FAILED.getCode();

            log.info("Payment processing completed. Response Code: {}", responseCode);
            return new BankPaymentResponse(responseCode);
        } catch (InterruptedException e) {
            log.error("Thread was interrupted during bank processing.", e);
            Thread.currentThread().interrupt();
            return new BankPaymentResponse(BankResponseCode.FAILED.getCode());
        }
    }

    /**
     * Generates a random delay between 2 to 7 seconds.
     */
    private int getRandomDelay() {
        int[] delays = {2000, 3000, 5000, 7000};
        return delays[random.nextInt(delays.length)];
    }
}
