package com.nlu.app.repository;

import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nlu.app.entity.Role;

@Repository
@Observed
public interface RoleRepository extends JpaRepository<Role, String> {}
