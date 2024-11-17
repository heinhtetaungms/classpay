package com.cp.classpay.repository;

import com.cp.classpay.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitlistRepo extends JpaRepository<Waitlist, Long> {
}
