package com.cp.classpay.entity;

import com.cp.classpay.utils.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "refunds")
@Getter
@Setter
public class Refund extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "user_package_id", nullable = false)
    private UserPackage userPackage;

    private int creditRefunded;

    private String reason;

    @Column(nullable = false)
    private ZonedDateTime refundTime;
}
