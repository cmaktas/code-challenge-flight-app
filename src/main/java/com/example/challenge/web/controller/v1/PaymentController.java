package com.example.challenge.web.controller.v1;

import com.example.challenge.service.payment.PaymentService;
import com.example.challenge.web.model.v1.request.PaymentRequest;
import com.example.challenge.web.model.v1.response.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Controller", description = "API for processing payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Purchase a seat", description = "Allows a user to purchase a seat.")
    @ApiResponse(responseCode = "200", description = "Payment processed asynchronously")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "409", description = "Seat already purchased")
    @PostMapping
    public ResponseEntity<PaymentResponse> purchaseSeat(@Valid @RequestBody PaymentRequest paymentRequest) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(paymentService.purchaseSeat(paymentRequest));
    }

    /**
     * Retrieves the current status of a payment by its ID.
     */
    @Operation(summary = "Get payment status by ID", description = "Allows the client to check the latest status of the payment, e.g. PENDING, SUCCESS, or FAILED.")
    @ApiResponse(responseCode = "200", description = "Payment status retrieved")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(id));
    }

}
