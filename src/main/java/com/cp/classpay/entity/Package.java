package com.cp.classpay.entity;

import com.cp.classpay.utils.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "packages")
@Getter
@Setter
public class Package extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packageId;

    private String packageName;
    private Integer totalCredits;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private Integer expiryDays;
    private String country;
}
