package com.cp.classpay.service.cache;

import com.cp.classpay.entity.Refund;
import com.cp.classpay.repository.RefundRepo;
import com.cp.classpay.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RefundCacheService {

    private final RefundRepo refundRepo;
    private final RedisUtil redisUtil;

    public RefundCacheService(RefundRepo refundRepo, RedisUtil redisUtil) {
        this.refundRepo = refundRepo;
        this.redisUtil = redisUtil;
    }

    @Value("${app.redis.refund_e.key_prefix}")
    private String refund_e_key_prefix;
    @Value("${app.redis.package_e.key_ttl}")
    private long refund_e_key_ttl;

    public Refund save(Refund refund) {
        Refund record = refundRepo.save(refund);

        String key = refund_e_key_prefix + record.getRefundId();
        set(key, record);

        return record;
    }

    public Refund findById(long packageId) {
        String key = refund_e_key_prefix + packageId;
        Refund record = redisUtil.getHash(key, Refund.class);

        if (record == null) {
            record = refundRepo.findByRefundId(packageId);
            set(key, record);
        }
        return record;
    }

    private void set(String key, Refund refund) {
        redisUtil.setHash(key, refund, refund_e_key_ttl, TimeUnit.MINUTES);
    }
}
