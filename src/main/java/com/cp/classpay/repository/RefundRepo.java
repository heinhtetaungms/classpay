package com.cp.classpay.repository;

import com.cp.classpay.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepo extends JpaRepository<Refund, Long> {
    default Refund findByRefundId(long id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("Refund not found by id: " + id));
    }
}
