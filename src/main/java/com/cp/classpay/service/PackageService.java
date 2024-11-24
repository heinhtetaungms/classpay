package com.cp.classpay.service;

import com.cp.classpay.api.input.package_.PurchasePackageRequest;
import com.cp.classpay.api.output.package_.PackageResponse;
import com.cp.classpay.api.output.package_.PurchasePackageResponse;
import com.cp.classpay.api.output.package_.UserPackageResponse;

import java.util.List;

public interface PackageService {
    List<PackageResponse> getAvailablePackagesByCountry(String country);
    PurchasePackageResponse purchasePackage(String jwtToken, PurchasePackageRequest purchasePackageRequest);
    List<UserPackageResponse> getUserPackages(Long userId);
}
