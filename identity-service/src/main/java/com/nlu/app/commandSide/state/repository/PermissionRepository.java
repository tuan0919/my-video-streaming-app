package com.nlu.app.querySide.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nlu.app.querySide.entity.Permission;

import feign.Param;

@Repository
// @Observed
public interface PermissionRepository extends JpaRepository<Permission, String> {
    @Query("SELECT CASE WHEN COUNT(e) = :totalNames THEN true ELSE false END FROM Permission e WHERE e.name IN :names")
    boolean allExistsById(@Param("names") Set<String> names, @Param("totalNames") Long totalNames);
}
