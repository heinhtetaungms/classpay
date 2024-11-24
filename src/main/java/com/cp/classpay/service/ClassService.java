package com.cp.classpay.service;

import com.cp.classpay.api.input.class_.ClassRegistrationRequest;
import com.cp.classpay.api.output.class_.ClassRegistrationResponse;
import com.cp.classpay.api.output.class_.ClassResponse;

import java.util.List;

public interface ClassService {
    ClassRegistrationResponse registerClass(ClassRegistrationRequest classRegistrationRequest);
    List<ClassResponse> getAvailableClassesByCountry(String country);
    void when_class_end_waitlist_user_credit_need_to_be_refunded();
}
