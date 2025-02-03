package com.example.challenge.web.model.v1.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    @Schema(description = "Status of the payment", example = "SUCCESS")
    private String status;

    @Schema(description = "Message providing additional information", example = "Seat purchased successfully.")
    private String message;

    @Schema(description = "Unique ID of the payment record", example = "123")
    private Long paymentId;

    @Schema(description = "URL to check the current status of this payment", example = "/api/v1/payments/123")
    private String statusCheckUrl;

}