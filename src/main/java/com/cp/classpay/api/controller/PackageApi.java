package com.cp.classpay.api.controller;

import com.cp.classpay.api.input.package_.PackageRegisterRequest;
import com.cp.classpay.api.input.package_.PurchasePackageRequest;
import com.cp.classpay.api.output.package_.PackageRegisterResponse;
import com.cp.classpay.api.output.package_.PackageResponse;
import com.cp.classpay.api.output.package_.PurchasePackageResponse;
import com.cp.classpay.api.output.package_.UserPackageResponse;
import com.cp.classpay.service.PackageService;
import com.cp.classpay.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/packages")
public class PackageApi {

    private final PackageService packageService;

    public PackageApi(PackageService packageService) {
        this.packageService = packageService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PackageRegisterResponse>> registerPackage(@Validated @RequestBody PackageRegisterRequest packageRegisterRequest, BindingResult result) {
        PackageRegisterResponse packageRegisterResponse = packageService.registerPackage(packageRegisterRequest);
        return ApiResponse.of(packageRegisterResponse);
    }

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

    @GetMapping("/purchased-packages")
    public ResponseEntity<ApiResponse<List<UserPackageResponse>>> getUserPurchasedPackages(@RequestParam Long userId, @RequestParam String country) {
        List<UserPackageResponse> userPurchasedPackages = packageService.getPurchasedPackagesByUserIdAndCountry(userId, country);
        return ApiResponse.of(userPurchasedPackages);
    }
}
