package com.cp.classpay.repository;

import com.cp.classpay.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepo extends JpaRepository<Package, Long> {
    List<Package> findAllByCountry(String country);
}
