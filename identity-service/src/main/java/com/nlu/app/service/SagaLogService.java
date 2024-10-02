package com.nlu.app.service;

import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.saga.Saga;
import com.nlu.app.saga.SagaLog;
import com.nlu.app.repository.SagaLogRepository;
import com.nlu.app.repository.SagaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SagaLogService {
    SagaLogRepository sagaLogRepository;
    SagaRepository sagaRepository;

    public synchronized boolean checkAllSteps(String sagaId, Set<String> stepSet) {
        Set<SagaLog> sagaLogs = sagaLogRepository
                .findSagaLogsBySagaIdAndSagaStepInAndStatusEqualsIgnoreCase(sagaId, stepSet, SagaStatus.SUCCESS);
        // Lọc ra các sagaStep đã xảy ra
        Set<String> happenedSteps = sagaLogs.stream()
                .map(SagaLog::getSagaStep)
                .collect(Collectors.toSet());
        // So sánh trực tiếp 2 tập hợp để kiểm tra xem tất cả các bước có xảy ra hay không
        return happenedSteps.containsAll(stepSet);
    }

    private String getFailedStep(String sagaId) {
        var log = sagaLogRepository.findSagaLogByStatusIsAndSagaIdIs(SagaStatus.FAILED, sagaId);
        if (log == null) return null;
        return log.getSagaStep();
    }

    @Transactional
    public synchronized void addSagaLog (SagaLog sLog, Set<String> proceedSet, Map<String, List<String>> compensationMap) {
        if (sagaLogRepository.findById(sLog.getId()).isEmpty()) {
            sagaLogRepository.save(sLog);
            updateSaga(sLog.getSagaId(), sLog.getSagaAction(), proceedSet, compensationMap);
        }
    }

    public synchronized void updateSaga(String sagaId, String sagaAction, Set<String> processSet, Map<String, List<String>> compensationMap) {
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
            if (checkAllSteps(sagaId, processSet)) {
                saga.setState("COMPLETED");
                saga.setCurrentStep("NONE");
                // TODO: do something to announce
                log.info("{} saga is successfully completed.", saga.getSagaAction());
            } else {
                String failedStep = getFailedStep(sagaId);
                if (failedStep == null) {
                    saga.setState("PROCESSING");
                } else {
                    Set<String> compensationSet = compensationMap
                            .get(failedStep)
                            .stream().collect(Collectors.toSet());
                    if (checkAllSteps(sagaId, compensationSet)) {
                        saga.setCurrentStep("NONE");
                        saga.setState("ABORTED");
                        // TODO: do something to announce
                        log.info("{} saga is safely aborted.", saga.getSagaAction());
                    }
                    else
                        saga.setState("ABORTING");
                }
            }
            saga.setUpdateAt(LocalDateTime.now());
            sagaRepository.save(saga);
        }
    }
}
