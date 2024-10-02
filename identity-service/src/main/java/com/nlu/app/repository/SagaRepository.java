package com.nlu.app.repository;

import com.nlu.app.saga.Saga;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaRepository extends JpaRepository<Saga, String> {

}
