package com.example.challenge.service.payment;

import com.example.challenge.domain.entity.Flight;
import com.example.challenge.domain.entity.Payment;
import com.example.challenge.domain.entity.Seat;
import com.example.challenge.domain.enums.BankResponseCode;
import com.example.challenge.domain.enums.PaymentStatus;
import com.example.challenge.domain.enums.SeatStatus;
import com.example.challenge.repository.FlightRepository;
import com.example.challenge.repository.PaymentRepository;
import com.example.challenge.repository.SeatRepository;
import com.example.challenge.service.BankService;
import com.example.challenge.web.model.v1.request.BankPaymentRequest;
import com.example.challenge.web.model.v1.request.PaymentRequest;
import com.example.challenge.web.model.v1.response.BankPaymentResponse;
import com.example.challenge.web.model.v1.response.PaymentResponse;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
@ActiveProfiles("test")
class PaymentFlowIntegrationTest {

    @TestConfiguration
    static class TestBankServiceConfiguration {
        @Bean
        @Primary
        public BankService bankService() {
            return new BankService() {
                @Override
                public BankPaymentResponse pay(BankPaymentRequest request) {
                    return new BankPaymentResponse(BankResponseCode.SUCCESS.getCode());
                }
            };
        }
    }

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    // TestRestTemplate to simulate HTTP calls (i.e. via PaymentController).
    @Autowired
    private TestRestTemplate restTemplate;

    private Seat testSeat;

    @BeforeEach
    void setUp() {
        // Create and persist a dummy Flight so that Seat.flight is not null.
        Flight dummyFlight = Flight.builder()
                .flightNumber("TEST123")
                .origin("TestOrigin")
                .destination("TestDestination")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .seatCapacity(1)
                .build();
        dummyFlight = flightRepository.saveAndFlush(dummyFlight);

        // Create and persist a test seat associated with the dummy flight.
        testSeat = Seat.builder()
                .seatNumber("A1")
                .price(BigDecimal.valueOf(150.00))
                .status(SeatStatus.AVAILABLE)
                .flight(dummyFlight)
                .build();
        testSeat = seatRepository.saveAndFlush(testSeat);
    }

    @Test
    void purchaseSeatIntegration_ShouldCreatePendingPayment() {
        // Build a PaymentRequest using the test seat.
        PaymentRequest request = PaymentRequest.builder()
                .seatId(testSeat.getId())
                .price(testSeat.getPrice())
                .build();

        // Simulate an HTTP POST to /api/v1/payments.
        ResponseEntity<PaymentResponse> responseEntity =
                restTemplate.postForEntity("/api/v1/payments", request, PaymentResponse.class);
        PaymentResponse response = responseEntity.getBody();

        // Assert that a pending payment was created.
        assertNotNull(response);
        assertNotNull(response.getPaymentId());
        assertEquals(PaymentStatus.PENDING.name(), response.getStatus());

        Payment stored = paymentRepository.findById(response.getPaymentId()).orElseThrow();
        assertEquals(testSeat.getId(), stored.getSeat().getId());
        assertEquals(0, stored.getPrice().compareTo(testSeat.getPrice()));

    }

    @Test
    void getPaymentStatusIntegration_ShouldReturnCorrectStatus() {
        // Build a PaymentRequest and post it.
        PaymentRequest request = PaymentRequest.builder()
                .seatId(testSeat.getId())
                .price(testSeat.getPrice())
                .build();

        ResponseEntity<PaymentResponse> purchaseResponseEntity =
                restTemplate.postForEntity("/api/v1/payments", request, PaymentResponse.class);
        Long paymentId = purchaseResponseEntity.getBody().getPaymentId();

        Awaitility.await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            PaymentResponse statusResponse =
                    restTemplate.getForObject("/api/v1/payments/{id}", PaymentResponse.class, paymentId);
            assertEquals(PaymentStatus.SUCCESS.name(), statusResponse.getStatus());
        });
    }

    @Test
    void concurrentPaymentRequests_ForSameSeat_OneSucceedsAndOtherFails() throws Exception {
        // Ensure the seat is available.
        testSeat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.saveAndFlush(testSeat);

        PaymentRequest request = PaymentRequest.builder()
                .seatId(testSeat.getId())
                .price(testSeat.getPrice())
                .build();

        // Use TestRestTemplate to simulate two concurrent POST requests.
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<ResponseEntity<PaymentResponse>> task = () ->
                    restTemplate.postForEntity("/api/v1/payments", request, PaymentResponse.class);
            Future<ResponseEntity<PaymentResponse>> future1 = executor.submit(task);
            Future<ResponseEntity<PaymentResponse>> future2 = executor.submit(task);

            PaymentResponse resp1 = future1.get().getBody();
            PaymentResponse resp2 = future2.get().getBody();

            // Wait until asynchronous processing finishes and assert that one payment is SUCCESS while the other is FAILED.
            Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                PaymentResponse status1 =
                        restTemplate.getForObject("/api/v1/payments/{id}", PaymentResponse.class, resp1.getPaymentId());
                PaymentResponse status2 =
                        restTemplate.getForObject("/api/v1/payments/{id}", PaymentResponse.class, resp2.getPaymentId());
                boolean oneSuccessOneFail = (status1.getStatus().equals(PaymentStatus.SUCCESS.name())
                        && status2.getStatus().equals(PaymentStatus.FAILED.name()))
                        || (status1.getStatus().equals(PaymentStatus.FAILED.name())
                        && status2.getStatus().equals(PaymentStatus.SUCCESS.name()));
                assertTrue(oneSuccessOneFail, "One payment should succeed and the other should fail.");
            });
        } finally {
            executor.shutdown();
        }
    }
}
