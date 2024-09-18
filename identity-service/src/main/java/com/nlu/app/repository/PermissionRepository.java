package com.nlu.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nlu.app.entity.Permission;

import io.micrometer.observation.annotation.Observed;

@Repository
@Observed
public interface PermissionRepository extends JpaRepository<Permission, String> {}
