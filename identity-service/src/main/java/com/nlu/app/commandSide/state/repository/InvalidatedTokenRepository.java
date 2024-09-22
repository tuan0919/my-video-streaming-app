package com.nlu.app.commandSide.state.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nlu.app.commandSide.state.entity.InvalidatedToken;

@Repository
// @Observed
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {}
