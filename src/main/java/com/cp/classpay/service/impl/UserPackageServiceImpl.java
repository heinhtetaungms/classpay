package com.cp.classpay.service.impl;

import com.cp.classpay.repository.UserPackageRepo;
import com.cp.classpay.service.UserPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class UserPackageServiceImpl implements UserPackageService {
    @Autowired
    private UserPackageRepo userPackageRepo;

    @Transactional
    @Override
    public int updateExpiredPackages() {
        ZonedDateTime currentDate = ZonedDateTime.now();
        int updatedCount = userPackageRepo.updateExpiredPackages(currentDate);
        return updatedCount;
    }
}
