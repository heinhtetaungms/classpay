package com.cp.classpay.service;

import com.cp.classpay.api.input.class_.ClassRegisterRequest;
import com.cp.classpay.api.output.class_.ClassRegisterResponse;
import com.cp.classpay.api.output.class_.ClassResponse;

import java.util.List;

public interface ClassService {
    ClassRegisterResponse registerClass(ClassRegisterRequest classRegisterRequest);
    List<ClassResponse> getAvailableClassesByCountry(String country);
    void refundWaitlistUserCreditsWhenClassEnd();
    void remindClassStartTimeToUser();
}
