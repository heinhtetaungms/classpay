package com.cp.classpay.api.controller;

import com.cp.classpay.api.input.class_.ClassRegistrationRequest;
import com.cp.classpay.api.output.class_.ClassRegistrationResponse;
import com.cp.classpay.api.output.class_.ClassResponse;
import com.cp.classpay.service.ClassService;
import com.cp.classpay.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classes")
public class ClassApi {
    @Autowired
    private ClassService classService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClassRegistrationResponse>> registerClass(@Validated @RequestBody ClassRegistrationRequest classRegistrationRequest, BindingResult result) {
        ClassRegistrationResponse classRegistrationResponse = classService.registerClass(classRegistrationRequest);
        return ApiResponse.of(classRegistrationResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getAvailableClassesByCountry(@RequestParam String country) {
        List<ClassResponse> availableClassesByCountry = classService.getAvailableClassesByCountry(country);
        return ApiResponse.of(availableClassesByCountry);
    }

}
