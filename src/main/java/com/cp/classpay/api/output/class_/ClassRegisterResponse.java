package com.cp.classpay.api.output.class_;

import com.cp.classpay.entity.Class;

import java.time.ZonedDateTime;

public record ClassRegisterResponse(
        Long classId,
        String className,
        String country,
        int requiredCredits,
        int availableSlots,
        ZonedDateTime classDate,
        String businessName
) {
        public static ClassRegisterResponse from(Class classEntity) {
                return new ClassRegisterResponse(
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

