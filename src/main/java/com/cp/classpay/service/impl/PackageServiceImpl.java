package com.cp.classpay.service.impl;

import com.cp.classpay.api.input.package_.PurchasePackageRequest;
import com.cp.classpay.api.output.package_.PackageResponse;
import com.cp.classpay.api.output.package_.PurchasePackageResponse;
import com.cp.classpay.api.output.package_.UserPackageResponse;
import com.cp.classpay.commons.enum_.PackageStatus;
import com.cp.classpay.entity.Package;
import com.cp.classpay.entity.User;
import com.cp.classpay.entity.UserPackage;
import com.cp.classpay.repository.PackageRepo;
import com.cp.classpay.repository.UserPackageRepo;
import com.cp.classpay.service.PackageService;
import com.cp.classpay.utils.EssentialUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackageServiceImpl implements PackageService {

    @Autowired
    private PackageRepo packageRepo;
    @Autowired
    private UserPackageRepo userPackageRepo;
    @Autowired
    private EssentialUtil essentialUtil;
    @Autowired
    private MockPaymentService mockPaymentService;

    @Override
    public List<PackageResponse> getAvailablePackagesByCountry(String country) {
        List<Package> packages = packageRepo.findAllByCountry(country);
        return packages.stream()
                .map(data -> PackageResponse.toPackageResponse(data))
                .toList();
    }

    @Override
    public PurchasePackageResponse purchasePackage(String jwtToken, PurchasePackageRequest purchasePackageRequest) {
        User user = essentialUtil.getUser(jwtToken);

        //Retrieve package by ID and check if it exists
        Package selectedPackage = packageRepo.findById(purchasePackageRequest.packageId())
                .orElseThrow(() -> new IllegalArgumentException("Package not found"));

        //Validate package country against user's country
        if (!selectedPackage.getCountry().equals(user.getCountry())) {
            throw new IllegalArgumentException("Package is not available for the user's country.");
        }

        boolean hasActivePackage = userPackageRepo.existsUserPackageByUser_UserIdAndPackageEntity_PackageIdAndStatus(user.getUserId(), purchasePackageRequest.packageId(), PackageStatus.ACTIVE);

        if (hasActivePackage) {
            throw new IllegalStateException("User already has an active package of this type.");
        }

        mockPaymentService.paymentCharge(selectedPackage, user);

        UserPackage userPackage = new UserPackage();
        userPackage.setUser(user);
        userPackage.setPackageEntity(selectedPackage);
        userPackage.setRemainingCredits(selectedPackage.getTotalCredits());
        userPackage.setStatus(PackageStatus.ACTIVE);

        userPackageRepo.save(userPackage);
        return PurchasePackageResponse.toPurchasePackageResponse(userPackage);
    }


    @Override
    public List<UserPackageResponse> getUserPackages(Long userId) {
        List<UserPackage> userPackages = userPackageRepo.findAllByUserUserId(userId);

        return userPackages.stream()
                .map(userPackage -> UserPackageResponse.toUserPackageResponse(userPackage))
                .collect(Collectors.toList());
    }

}
