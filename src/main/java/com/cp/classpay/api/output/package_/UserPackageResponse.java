package com.cp.classpay.api.output.package_;

import com.cp.classpay.entity.UserPackage;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record UserPackageResponse(
        Long packageId,
        String packageName,
        int remainingCredits,
        BigDecimal price,
        String country,
        boolean isExpired,
        ZonedDateTime expirationDate
) {
    public static UserPackageResponse toUserPackageResponse(UserPackage userPackage) {
        return new UserPackageResponse(
                userPackage.getPackageEntity().getPackageId(),
                userPackage.getPackageEntity().getPackageName(),
                userPackage.getRemainingCredits(),
                userPackage.getPackageEntity().getPrice(),
                userPackage.getPackageEntity().getCountry(),
                userPackage.getExpirationDate().isBefore(ZonedDateTime.now()),
                userPackage.getExpirationDate()
        );
    }
}