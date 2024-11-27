package com.cp.classpay.scheduler;

import com.cp.classpay.service.UserPackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PackageStatusScheduler {
    @Autowired
    private UserPackageService userPackageService;

    /**
     * Run daily at midnight to update expired packages.
     */
    //@Scheduled(cron = "0 0 0 * * ?")
    @Scheduled(cron = "0 */5 * * * *")
    public void updateExpiredPackagesJob() {
        int updatedCount = userPackageService.updateExpiredPackages();
        log.info("Updated expired packages job count: {}", updatedCount);
    }
}
