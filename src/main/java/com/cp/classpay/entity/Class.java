package com.cp.classpay.entity;

import com.cp.classpay.utils.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "classes")
@Getter
@Setter
public class Class extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classId;

    private String className;
    private String country;

    private Integer requiredCredits;
    private Integer availableSlots;

    @Column(nullable = false)
    private ZonedDateTime classDate;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}
