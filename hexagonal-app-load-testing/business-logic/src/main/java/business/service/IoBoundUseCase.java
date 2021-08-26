package business.service;

import business.entity.IoTask;
import business.external.MonitoringService;
import business.external.PersistenceRepository;
import business.port.BusinessInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// not made implementation of actual interface for sake of simplicity, so this concrete class itself is public API to caller
@BusinessInterface
public class IoBoundUseCase {

    private static final Logger log = LoggerFactory.getLogger(IoBoundUseCase.class);

    private final MonitoringService monitoringService;
    private final PersistenceRepository persistenceRepository;

    public IoBoundUseCase(MonitoringService monitoringService, PersistenceRepository persistenceRepository) {
        this.monitoringService = monitoringService;
        this.persistenceRepository = persistenceRepository;
    }

    // can pass around DTO rather than domain entity
    public boolean block(IoTask task) {

        try {
            // block the thread to simulate IO bound task
            log.debug("Waiting for {}", task);
            Thread.sleep(task.getDurationInMillis());

            persistenceRepository.persist(task);
            monitoringService.push(task.getMetric());
            return true;

        } catch (Exception e) {
            log.error("Error occurred while executing io task!", e);
            return false;
        }
    }
}