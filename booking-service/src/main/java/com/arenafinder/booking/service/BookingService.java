package com.arenafinder.booking.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.arenafinder.booking.dto.BookingDTOs.BookingResponse;
import com.arenafinder.booking.dto.BookingDTOs.CreateBookingRequest;
import com.arenafinder.booking.exception.BookingException.BookingNotFoundException;
import com.arenafinder.booking.exception.BookingException.SlotAlreadyBookedException;
import com.arenafinder.booking.exception.BookingException.UnauthorizedBookingAccessException;
import com.arenafinder.booking.model.Booking;
import com.arenafinder.booking.repository.BookingRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Long userId) {
        log.info("Creating booking for arena: {} by user: {}",
                request.getArenaId(), userId);

        // check slot not already booked
        boolean slotTaken = bookingRepository
                .existsByArenaIdAndDateAndStartTimeAndStatus(
                        request.getArenaId(),
                        request.getDate(),
                        request.getStartTime(),
                        Booking.Status.CONFIRMED);

        if (slotTaken) {
            throw new SlotAlreadyBookedException();
        }

        Booking booking = Booking.builder()
                .arenaId(request.getArenaId())
                .userId(userId)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(Booking.Status.CONFIRMED)
                .totalPrice(0.0) // PHASE 5: calculate from arena pricePerHour
                .build();

        booking = bookingRepository.save(booking);
        log.info("Booking created with ID: {}", booking.getId());
        return mapToResponse(booking);
    }

    public BookingResponse getBookingById(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedBookingAccessException();
        }
        return mapToResponse(booking);
    }

    public List<BookingResponse> getMyBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse cancelBooking(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedBookingAccessException();
        }

        booking.setStatus(Booking.Status.CANCELLED);
        booking = bookingRepository.save(booking);
        log.info("Booking {} cancelled", id);
        return mapToResponse(booking);
    }

    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setArenaId(booking.getArenaId());
        response.setUserId(booking.getUserId());
        response.setDate(booking.getDate());
        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());
        response.setStatus(booking.getStatus().name());
        response.setTotalPrice(booking.getTotalPrice());
        return response;
    }
}