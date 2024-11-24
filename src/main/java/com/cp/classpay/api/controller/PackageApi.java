package com.cp.classpay.api.controller;

import com.cp.classpay.api.input.package_.PurchasePackageRequest;
import com.cp.classpay.api.output.package_.PackageResponse;
import com.cp.classpay.api.output.package_.PurchasePackageResponse;
import com.cp.classpay.api.output.package_.UserPackageResponse;
import com.cp.classpay.service.PackageService;
import com.cp.classpay.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/packages")
public class PackageApi {
    @Autowired
    private PackageService packageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getAvailablePackages(@RequestParam String country) {
        List<PackageResponse> packages = packageService.getAvailablePackagesByCountry(country);
        return ApiResponse.of(packages);
    }

    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<PurchasePackageResponse>> purchasePackage(@RequestHeader("Authorization") String jwtToken, @Validated @RequestBody PurchasePackageRequest purchasePackageRequest, BindingResult result) {
        PurchasePackageResponse purchasePackageResponse = packageService.purchasePackage(jwtToken, purchasePackageRequest);
        return ApiResponse.of(purchasePackageResponse);
    }

    @GetMapping("/users/{userId}/purchased")
    public ResponseEntity<ApiResponse<List<UserPackageResponse>>> getUserPurchasedPackages(@PathVariable Long userId) {
        List<UserPackageResponse> userPurchasedPackages = packageService.getUserPackages(userId);
        return ApiResponse.of(userPurchasedPackages);
    }
}
