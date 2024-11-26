package com.cp.classpay.service.cache;

import com.cp.classpay.entity.Package;
import com.cp.classpay.repository.PackageRepo;
import com.cp.classpay.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PackageCacheService {

    private final PackageRepo packageRepo;
    private final RedisUtil redisUtil;

    public PackageCacheService(PackageRepo packageRepo, RedisUtil redisUtil) {
        this.packageRepo = packageRepo;
        this.redisUtil = redisUtil;
    }

    @Value("${app.redis.package_e.key_prefix}")
    private String package_e_key_prefix;
    @Value("${app.redis.package_e.key_ttl}")
    private long package_e_key_ttl;

    @Value("${app.redis.package_l.key_prefix}")
    private String package_l_key_prefix;
    @Value("${app.redis.package_l.key_ttl}")
    private long package_l_key_ttl;

    public Package save(Package package_e) {
        Package record = packageRepo.save(package_e);

        String key = package_e_key_prefix + record.getPackageId();
        set(key, record);

        update_available_package_list_by_country(record);

        return record;
    }

    public Package findById(long packageId) {
        String key = package_e_key_prefix + packageId;
        Package record = redisUtil.getHash(key, Package.class);

        if (record == null) {
            record = packageRepo.findByPackageId(packageId);
            set(key, record);
        }
        return record;
    }

    public List<Package> findAllByCountry(String country) {
        String key = package_l_key_prefix + country;
        List<Package> recordList = redisUtil.getList(key, Package.class);

        if (recordList.isEmpty()) {
            recordList = packageRepo.findAllByCountry(country);
            setList(key, recordList);
        }
        return recordList;
    }

    private List<Package> update_available_package_list_by_country(Package package_e) {
        String key = package_l_key_prefix + package_e.getCountry();
        List<Package> recordList = redisUtil.getList(key, Package.class);

        if (recordList.isEmpty()) {
            //will invoke db hit only once to consistence with db if redis key is deleted
            recordList = packageRepo.findAllByCountry(package_e.getCountry());
        }
        List<Package> updatedList = recordList.stream().filter(record -> !record.getPackageId().equals(package_e.getPackageId())).collect(Collectors.toList());

        updatedList.add(package_e);
        setList(key, updatedList);

        return updatedList;
    }

    private void set(String key, Package package_e) {
        redisUtil.setHash(key, package_e, package_e_key_ttl, TimeUnit.MINUTES);
    }

    private void setList(String key, List<Package> packageList) {
        redisUtil.setList(key, packageList, package_l_key_ttl, TimeUnit.MINUTES);
    }
}
