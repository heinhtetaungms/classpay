package com.cp.classpay.service.cache;

import com.cp.classpay.entity.UserPackage;
import com.cp.classpay.repository.UserPackageRepo;
import com.cp.classpay.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserPackageCacheService {
    @Autowired
    private UserPackageRepo userPackageRepo;
    @Autowired
    private RedisUtil redisUtil;

    private static final String PACKAGE_CACHE_KEY_PREFIX = "user_package:";

    public List<UserPackage> updateCacheForUserPackagesByCountry(Long userId, String userCountry) {
        String cacheKey = PACKAGE_CACHE_KEY_PREFIX + userId + ":" + userCountry;
        redisUtil.delete(cacheKey);
        List<UserPackage> updatedPackages = userPackageRepo.findAllByUser_UserIdAndUser_Country(userId, userCountry);
        cacheUserPackages(cacheKey, updatedPackages);
        return updatedPackages;
    }

    public List<UserPackage> getUserPackagesByCountry(Long userId, String userCountry) {
        String cacheKey = PACKAGE_CACHE_KEY_PREFIX + userId + ":" + userCountry;

        List<UserPackage> cachedPackages = redisUtil.getList(cacheKey, UserPackage.class);

        if (cachedPackages == null || cachedPackages.isEmpty()) {
            cachedPackages = userPackageRepo.findAllByUser_UserIdAndUser_Country(userId, userCountry);
            cacheUserPackages(cacheKey, cachedPackages);
        }

        return cachedPackages;
    }

    private void cacheUserPackages(String cacheKey, List<UserPackage> userPackages) {
        redisUtil.delete(cacheKey);
        redisUtil.setList(cacheKey, userPackages, 1, TimeUnit.MINUTES);
    }

}
