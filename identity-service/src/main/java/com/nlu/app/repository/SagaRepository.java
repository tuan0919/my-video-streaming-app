package com.nlu.app.repository;

import com.nlu.app.entity.Saga;
import com.nlu.app.entity.SagaLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface SagaRepository extends JpaRepository<Saga, String> {

}
