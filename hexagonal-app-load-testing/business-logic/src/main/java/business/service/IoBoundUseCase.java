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

    // complicated with generic because of different upstream caller needs
    public <R> R run(IoTask<R> task) {

        try {
            monitoringService.push(task.getMetric());
            persistenceRepository.persist(task);

            log.info("Waiting for {}", task);
            return task.execute();
        } catch (Exception e) {

            log.error("Error caught in use case!", e);
            return null;
        }
    }
}