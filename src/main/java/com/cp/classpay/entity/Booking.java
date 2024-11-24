package com.cp.classpay.entity;

import com.cp.classpay.commons.enum_.BookingStatus;
import com.cp.classpay.utils.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "booking")
    private List<BookingDetail> bookingDetails = new ArrayList<>();

    @Column(nullable = false)
    private ZonedDateTime bookingTime;

    private ZonedDateTime cancellationTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(nullable = false)
    private boolean isCanceled;
}
