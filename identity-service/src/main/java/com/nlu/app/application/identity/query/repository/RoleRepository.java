package com.nlu.app.application.identity.query.repository;

import com.nlu.app.application.identity.query.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// @Observed
public interface RoleRepository extends JpaRepository<Role, String> {}
