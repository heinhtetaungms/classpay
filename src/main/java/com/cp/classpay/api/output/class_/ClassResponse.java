package com.cp.classpay.api.output.class_;

import com.cp.classpay.entity.Class;

import java.time.ZonedDateTime;

public record ClassResponse(
        Long id,
        String className,
        String country,
        int requiredCredits,
        int availableSlots,
        ZonedDateTime classDate,
        String businessName
) {
    public static ClassResponse from(Class classEntity) {
        return new ClassResponse(
                classEntity.getClassId(),
                classEntity.getClassName(),
                classEntity.getCountry(),
                classEntity.getRequiredCredits(),
                classEntity.getAvailableSlots(),
                classEntity.getClassStartDate(),
                classEntity.getBusiness().getBusinessName()
        );
    }
}
