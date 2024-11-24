package com.cp.classpay.api.output.class_;

import com.cp.classpay.entity.Class;

import java.time.ZonedDateTime;

public record ClassRegistrationResponse(
        Long classId,
        String className,
        String country,
        int requiredCredits,
        int availableSlots,
        ZonedDateTime classDate,
        String businessName
) {
        public static ClassRegistrationResponse toClassRegistrationResponse(Class classEntity) {
                return new ClassRegistrationResponse(
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

