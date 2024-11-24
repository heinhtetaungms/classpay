package com.cp.classpay.entity;

import com.cp.classpay.commons.enum_.PackageStatus;
import com.cp.classpay.utils.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_packages")
@Getter
@Setter
public class UserPackage extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userPackageId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    private Package packageEntity;

    private int remainingCredits;

    @Enumerated(EnumType.STRING)
    private PackageStatus status;

    private ZonedDateTime expirationDate;

    @PrePersist
    private void setExpirationDate() {
        expirationDate = ZonedDateTime.now().plusDays(packageEntity.getExpiryDays());
    }
}
