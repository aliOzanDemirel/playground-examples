package app.config;

import business.common.KafkaAdapter;
import business.common.MongoDbAdapter;
import business.common.PrometheusAdapter;
import business.external.MonitoringService;
import business.external.PersistenceRepository;
import business.external.StreamService;
import business.service.CpuBoundUseCase;
import business.service.IoBoundUseCase;

import javax.enterprise.inject.Produces;
import javax.ws.rs.ext.Provider;

@Provider
public class Configuration {

    @Produces
    public PersistenceRepository persistenceRepository() {
        return new MongoDbAdapter();
    }

    @Produces
    public StreamService streamService() {
        return new KafkaAdapter();
    }

    @Produces
    public MonitoringService monitoringService() {
        return new PrometheusAdapter();
    }

    @Produces
    public IoBoundUseCase ioBoundUseCase(MonitoringService monitoringService, PersistenceRepository persistenceRepository) {
        return new IoBoundUseCase(monitoringService, persistenceRepository);
    }

    @Produces
    public CpuBoundUseCase cpuBoundUseCase(MonitoringService monitoringService, StreamService streamService) {
        return new CpuBoundUseCase(monitoringService, streamService);
    }
}
