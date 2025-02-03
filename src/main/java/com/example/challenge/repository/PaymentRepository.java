package com.example.challenge.repository;

import com.example.challenge.domain.entity.Payment;
import com.example.challenge.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsBySeatIdAndStatus(Long seatId, PaymentStatus status);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Payment p " +
            "WHERE p.seat.id = :seatId " +
            "  AND p.createdAt < :newPaymentCreatedAt " +
            "  AND (p.status = 'PENDING' OR p.status = 'WAITING')")
    boolean existsBySeatIdAndOlderPending(@Param("seatId") Long seatId,
                                          @Param("newPaymentCreatedAt") LocalDateTime newPaymentCreatedAt);

    @Query("SELECT p FROM Payment p " +
            "WHERE p.seat.id = :seatId " +
            "  AND p.status IN :statuses")
    List<Payment> findAllBySeatIdAndStatusIn(@Param("seatId") Long seatId,
                                             @Param("statuses") Collection<PaymentStatus> statuses);

    Payment findFirstBySeatIdAndStatusOrderByCreatedAtAsc(Long seatId, PaymentStatus status);

}
