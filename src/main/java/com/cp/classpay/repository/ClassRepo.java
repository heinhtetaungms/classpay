package com.cp.classpay.repository;

import com.cp.classpay.entity.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassRepo extends JpaRepository<Class, Long> {
}
