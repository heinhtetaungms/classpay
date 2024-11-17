package com.cp.classpay.repository;

import com.cp.classpay.entity.UserPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPackageRepo extends JpaRepository<UserPackage, Long> {
}
