package com.nlu.app.commandSide.state.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nlu.app.commandSide.state.entity.Role;

@Repository
// @Observed
public interface RoleRepository extends JpaRepository<Role, String> {}
