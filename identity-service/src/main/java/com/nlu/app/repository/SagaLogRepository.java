package com.nlu.app.repository;

import com.nlu.app.saga.SagaLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface SagaLogRepository extends JpaRepository<SagaLog, String> {
    Set<SagaLog> findSagaLogsBySagaIdAndSagaStepInAndStatusEqualsIgnoreCase(String sagaId, Set<String> successStep, String status);
    SagaLog findTopBySagaIdOrderByUpdateAtDesc(String sagaId);
    SagaLog findSagaLogByStatusIsAndSagaIdIs(String status, String sagaId);
}
