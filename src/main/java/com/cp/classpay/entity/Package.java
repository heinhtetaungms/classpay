package com.cp.classpay.entity;

import com.cp.classpay.utils.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "packages")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Package extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packageId;

    private String packageName;
    private int totalCredits;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private int expiryDays;
    private String country;
}
