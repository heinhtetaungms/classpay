package com.cp.classpay.entity;

import com.cp.classpay.utils.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "classes")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Class extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classId;

    private String className;
    private String country;

    private int requiredCredits;
    private int availableSlots;

    @Column(nullable = false)
    private ZonedDateTime classStartDate;

    @Column(nullable = false)
    private ZonedDateTime classEndDate;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}
