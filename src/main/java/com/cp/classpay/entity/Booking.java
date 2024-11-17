package com.cp.classpay.entity;

import com.cp.classpay.utils.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;

    @ManyToOne
    @JoinColumn(name = "user_package_id", nullable = false)
    private UserPackage userPackage;

    @Column(nullable = false)
    private ZonedDateTime bookingTime;

    private ZonedDateTime cancellationTime;

    private String status;

    @Column(nullable = false)
    private boolean isCanceled;
}
