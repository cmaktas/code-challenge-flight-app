# flight-app code challenge

Thank you for applying to work in ***** Engineering.

As part of our interview process, we expect you to complete a coding challenge in order for us to understand your coding skills. 
The challenge is a Java11 + Spring Boot project which uses H2 as the database.


# Question 1: Flight Booking System

Most of the merchants sell products or services online. Flight ticket is one of these services.
For flight booking system the necessary REST services are listed below. We kindly ask you to implement them.

## Requirements

* Flight -> adding, removing and updating
* Seat for existing flight -> adding, removing or updating services. 
* Flight/Seat listing service which returns flight name, description, available seats and price.
* Payment service for the end user to buy their selected seat.
* A seat should not be sold to two passengers.
* If there are 2 passengers pay at the same time for the same seat, first successful should buy the seat and the 2nd one should fail with an appropriate message. We expect IT test for this case.
* No front end is necessary.
* Test coverage for the implemented service should be above 80%. We expect both Integration and unit tests.
* We expect Production Grade solution
* Bonus: ****** payment integration can be implemented for payment step.


# Question 2 : Latency Management

***** provides its payment service by calling bank endpoints. The bank responses are persisted to database.In [PaymentServiceTest.java]
class we have simulated 100 customers calling the payment service.

```java
    public void pay(BigDecimal price) {
        //pay with bank
        BankPaymentRequest request = new BankPaymentRequest();
        request.setPrice(price);
        BankPaymentResponse response = bankService.pay(request);

        //insert records
        Payment payment = new Payment();
        payment.setBankResponse(response.getResultCode());
        payment.setPrice(price);
        paymentRepository.save(payment);
        logger.info("Payment saved successfully!");
    }
```

In the simulation for some reason the bank response times take ~5 seconds. Due to this latency, a database connection problem is encountered after some time. (Running the [PaymentServiceTest.java]
class displays "Connection is not available, request timed out after 30005ms." error after some time.)

Find a way to persist bank responses to the database in this situation.

## Requirements

* DB connection pool must stay the same.
* DatabaseConfiguration.java, BankService.java, PaymentServiceClients.java and PaymentServiceTest.java classes must not be changed.
* In case of an error, there must not be any inconsistent data in the database.





