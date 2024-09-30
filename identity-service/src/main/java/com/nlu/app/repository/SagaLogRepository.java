package com.nlu.app.repository;

import com.nlu.app.entity.SagaLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface SagaLogRepository extends JpaRepository<SagaLog, String> {
    Set<SagaLog> findSagaLogsBySagaIdAndSagaStepInAndStatusEqualsIgnoreCase(String sagaId, Set<String> successStep, String status);
    SagaLog findTopBySagaIdOrderByUpdateAtDesc(String sagaId);
}
