package com.example.challenge.repository;

import com.example.challenge.domain.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("SELECT f FROM Flight f LEFT JOIN FETCH f.seats WHERE f.id = :flightId")
    Optional<Flight> findByIdWithSeats(@Param("flightId") Long flightId);

    @Query("SELECT DISTINCT f FROM Flight f LEFT JOIN FETCH f.seats")
    List<Flight> findAllWithSeats();
}
