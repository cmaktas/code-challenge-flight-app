package com.example.challenge.service.payment;

import com.example.challenge.web.model.v1.request.PaymentRequest;
import com.example.challenge.web.model.v1.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse purchaseSeat(PaymentRequest paymentRequest);
    PaymentResponse getPaymentStatus(Long paymentId);
}
