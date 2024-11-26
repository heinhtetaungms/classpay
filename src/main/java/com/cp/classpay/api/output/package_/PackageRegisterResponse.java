package com.cp.classpay.api.output.package_;

import com.cp.classpay.entity.Package;

import java.math.BigDecimal;

public record PackageRegisterResponse(
        String packageName,
        int totalCredits,
        BigDecimal price,
        int expiryDays,
        String country
) {
        public static PackageRegisterResponse from(Package package_e) {
                return new PackageRegisterResponse(
                        package_e.getPackageName(),
                        package_e.getTotalCredits(),
                        package_e.getPrice(),
                        package_e.getExpiryDays(),
                        package_e.getCountry()
                );
        }
}

