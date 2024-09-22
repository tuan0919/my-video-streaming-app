package com.nlu.app.querySide.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nlu.app.querySide.entity.InvalidatedToken;

@Repository
// @Observed
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {}
