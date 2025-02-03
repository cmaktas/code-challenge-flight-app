package com.example.challenge.service.payment.event;

import com.example.challenge.domain.entity.Payment;
import lombok.Value;

@Value
public class PaymentProcessEvent {
    Payment proceedPayment;
}
