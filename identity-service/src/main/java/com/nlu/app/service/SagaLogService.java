package com.nlu.app.service;

import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.entity.Saga;
import com.nlu.app.entity.SagaLog;
import com.nlu.app.repository.SagaLogRepository;
import com.nlu.app.repository.SagaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SagaLogService {
    SagaLogRepository sagaLogRepository;
    SagaRepository sagaRepository;

    public synchronized boolean checkSagaDone(String sagaId, Set<String> stepSet) {
        Set<SagaLog> sagaLogs = sagaLogRepository
                .findSagaLogsBySagaIdAndSagaStepInAndStatusEqualsIgnoreCase(sagaId, stepSet, SagaStatus.SUCCESS);
        // Lọc ra các sagaStep đã xảy ra
        Set<String> happenedSteps = sagaLogs.stream()
                .map(SagaLog::getSagaStep)
                .collect(Collectors.toSet());
        // So sánh trực tiếp 2 tập hợp để kiểm tra xem tất cả các bước có xảy ra hay không
        return happenedSteps.containsAll(stepSet);
    }

    public synchronized boolean checkSagaAborted(String sagaId, Set<String> abortSet) {
        Set<SagaLog> sagaLogs = sagaLogRepository
                .findSagaLogsBySagaIdAndSagaStepInAndStatusEqualsIgnoreCase(sagaId, abortSet, SagaStatus.SUCCESS);
        // Lọc ra các sagaStep đã xảy ra
        Set<String> happenedSteps = sagaLogs.stream()
                .map(SagaLog::getSagaStep)
                .collect(Collectors.toSet());
        // So sánh trực tiếp 2 tập hợp để kiểm tra xem tất cả các bước abort có xảy ra hay không
        return happenedSteps.containsAll(abortSet);
    }

    @Transactional
    public synchronized void addSagaLog (SagaLog sLog, Set<String> abortSet, Set<String> processSet) {
        if (sagaLogRepository.findById(sLog.getId()).isEmpty()) {
            sagaLogRepository.save(sLog);
            updateSaga(sLog.getSagaId(), sLog.getSagaAction(), abortSet, processSet);
        }
    }

    public synchronized void updateSaga(String sagaId, String sagaAction, Set<String> abortSet, Set<String> processSet) {
        if (sagaRepository.findById(sagaId).isEmpty()) {
            var saga = Saga.builder()
                    .sagaId(sagaId)
                    .currentStep("NONE")
                    .state("STARTED")
                    .sagaAction(sagaAction)
                    .createAt(LocalDateTime.now())
                    .updateAt(LocalDateTime.now())
                    .build();
            sagaRepository.save(saga);
            return;
        }
        // update saga tương ứng
        var sagaLog = sagaLogRepository.findTopBySagaIdOrderByUpdateAtDesc(sagaId);
        // Saga của log này đã tồn tại vì đã có log của nó
        if (sagaLog != null) {
            var saga = sagaRepository.findById(sagaId).get();
            String currentStep = sagaLog.getSagaStep();
            saga.setCurrentStep(currentStep);
            if (checkSagaDone(sagaId, processSet)) {
                saga.setState("COMPLETED");
                log.info("{} saga is successfully completed.", saga.getSagaAction());
            } else if (checkSagaAborted(sagaId, abortSet)) {
                saga.setState("ABORTED");
                log.info("{} saga is safely aborted.", saga.getSagaAction());
            } else {
                if (abortSet.contains(currentStep) && !checkSagaAborted(sagaId, abortSet)) {
                    saga.setState("ABORTING");
                }
                else
                    saga.setState("PROCESSING");
            }
            saga.setUpdateAt(LocalDateTime.now());
            sagaRepository.save(saga);
        }
    }
}
