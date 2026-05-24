package com.arenafinder.booking.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arenafinder.booking.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByArenaId(Long arenaId);

    // check if slot is already booked
    boolean existsByArenaIdAndDateAndStartTimeAndStatus(
            Long arenaId,
            LocalDate date,
            String startTime,
            Booking.Status status);
}