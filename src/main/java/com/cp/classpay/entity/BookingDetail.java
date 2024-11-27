package com.cp.classpay.entity;

import com.cp.classpay.utils.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bookings_detail")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDetail extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingDetailId;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "user_package_id", nullable = false)
    private UserPackage userPackage;

    private int creditsDeducted;
}
