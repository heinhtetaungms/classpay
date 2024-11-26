package com.cp.classpay.service;

import com.cp.classpay.api.input.class_.ClassRegisterRequest;
import com.cp.classpay.api.output.class_.ClassRegisterResponse;
import com.cp.classpay.api.output.class_.ClassResponse;

import java.util.List;

public interface ClassService {
    ClassRegisterResponse registerClass(ClassRegisterRequest classRegisterRequest);
    List<ClassResponse> getAvailableClassesByCountry(String country);
    void when_class_end_waitlist_user_credit_need_to_be_refunded();
}
