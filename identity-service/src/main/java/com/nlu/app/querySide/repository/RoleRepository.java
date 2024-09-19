package com.nlu.app.querySide.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nlu.app.querySide.entity.Role;

import io.micrometer.observation.annotation.Observed;

@Repository
@Observed
public interface RoleRepository extends JpaRepository<Role, String> {}
