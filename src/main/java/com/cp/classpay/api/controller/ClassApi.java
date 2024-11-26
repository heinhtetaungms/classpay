package com.cp.classpay.api.controller;

import com.cp.classpay.api.input.class_.ClassRegisterRequest;
import com.cp.classpay.api.output.class_.ClassRegisterResponse;
import com.cp.classpay.api.output.class_.ClassResponse;
import com.cp.classpay.service.ClassService;
import com.cp.classpay.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classes")
public class ClassApi {

    private final ClassService classService;

    public ClassApi(ClassService classService) {
        this.classService = classService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClassRegisterResponse>> registerClass(@Validated @RequestBody ClassRegisterRequest classRegisterRequest, BindingResult result) {
        ClassRegisterResponse classRegisterResponse = classService.registerClass(classRegisterRequest);
        return ApiResponse.of(classRegisterResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getAvailableClassesByCountry(@RequestParam String country) {
        List<ClassResponse> availableClassesByCountry = classService.getAvailableClassesByCountry(country);
        return ApiResponse.of(availableClassesByCountry);
    }
}
