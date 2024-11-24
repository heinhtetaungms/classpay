package com.cp.classpay.repository;

import com.cp.classpay.commons.enum_.BookingStatus;
import com.cp.classpay.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUser_UserIdAndStatus(Long userId, BookingStatus status);
    List<Booking> findAllByClassEntity_ClassIdAndStatus(Long classId, BookingStatus status);
}
