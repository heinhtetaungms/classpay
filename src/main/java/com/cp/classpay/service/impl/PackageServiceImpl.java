package com.cp.classpay.service.impl;

import com.cp.classpay.api.input.package_.PackageRegisterRequest;
import com.cp.classpay.api.input.package_.PurchasePackageRequest;
import com.cp.classpay.api.output.package_.PackageRegisterResponse;
import com.cp.classpay.api.output.package_.PackageResponse;
import com.cp.classpay.api.output.package_.PurchasePackageResponse;
import com.cp.classpay.api.output.package_.UserPackageResponse;
import com.cp.classpay.commons.enum_.PackageStatus;
import com.cp.classpay.entity.Package;
import com.cp.classpay.entity.User;
import com.cp.classpay.entity.UserPackage;
import com.cp.classpay.repository.UserPackageRepo;
import com.cp.classpay.service.PackageService;
import com.cp.classpay.service.cache.PackageCacheService;
import com.cp.classpay.service.cache.UserCacheService;
import com.cp.classpay.service.cache.UserPackageCacheService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackageServiceImpl implements PackageService {


    private final UserPackageRepo userPackageRepo;
    private final MockPaymentService mockPaymentService;
    private final PackageCacheService packageCacheService;
    private final UserPackageCacheService userPackageCacheService;
    private final UserCacheService userCacheService;

    public PackageServiceImpl(UserPackageRepo userPackageRepo, MockPaymentService mockPaymentService, PackageCacheService packageCacheService, UserPackageCacheService userPackageCacheService, UserCacheService userCacheService) {
        this.userPackageRepo = userPackageRepo;
        this.mockPaymentService = mockPaymentService;
        this.packageCacheService = packageCacheService;
        this.userPackageCacheService = userPackageCacheService;
        this.userCacheService = userCacheService;
    }

    @Override
    public PackageRegisterResponse registerPackage(PackageRegisterRequest packageRegisterRequest) {
        Package package_e = Package.builder()
                            .packageName(packageRegisterRequest.packageName())
                            .totalCredits(packageRegisterRequest.totalCredits())
                            .price(packageRegisterRequest.price())
                            .expiryDays(packageRegisterRequest.expiryDays())
                            .country(packageRegisterRequest.country())
                            .build();
        Package savedPackage = packageCacheService.save(package_e);
        return PackageRegisterResponse.from(savedPackage);
    }

    @Override
    public List<PackageResponse> getAvailablePackagesByCountry(String country) {
        List<Package> packages = packageCacheService.findAllByCountry(country);
        return packages.stream()
                .map(data -> PackageResponse.from(data))
                .toList();
    }

    @Override
    public PurchasePackageResponse purchasePackage(PurchasePackageRequest purchasePackageRequest) {
        User user = userCacheService.getUser();

        //Retrieve package by ID and check if it exists
        Package selectedPackage = packageCacheService.findById(purchasePackageRequest.packageId());

        //Validate package country against user's country
        if (!selectedPackage.getCountry().equals(user.getCountry())) {
            throw new IllegalArgumentException("Package is not available for the user's country.");
        }

        //TODO: to refactor this db hit & consider logic
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

        userPackageCacheService.save(userPackage);
        return PurchasePackageResponse.from(userPackage);
    }


    @Override
    public List<UserPackageResponse> getPurchasedPackagesByUserIdAndCountry(Long userId, String country) {
        List<UserPackage> userPackageList = userPackageCacheService.findUserPackagesByUserIdAndCountry(userId, country);
        return userPackageList.stream()
                .map(userPackage -> UserPackageResponse.from(userPackage))
                .collect(Collectors.toList());
    }

}
