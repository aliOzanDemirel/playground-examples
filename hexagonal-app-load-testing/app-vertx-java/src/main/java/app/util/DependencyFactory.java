package app.util;

import business.external.MonitoringService;
import business.external.PersistenceRepository;
import business.external.StreamService;
import business.service.CpuBoundUseCase;
import business.service.IoBoundUseCase;
import business.common.KafkaAdapter;
import business.common.MongoDbAdapter;
import business.common.PrometheusAdapter;

public class DependencyFactory {

    private final static PersistenceRepository persistenceRepository;
    private final static StreamService streamService;
    private final static MonitoringService monitoringService;
    private final static IoBoundUseCase ioBoundUseCase;
    private final static CpuBoundUseCase cpuBoundUseCase;

    static {
        persistenceRepository = new MongoDbAdapter();
        streamService = new KafkaAdapter();
        monitoringService = new PrometheusAdapter();
        ioBoundUseCase = new IoBoundUseCase(monitoringService, persistenceRepository);
        cpuBoundUseCase = new CpuBoundUseCase(monitoringService, streamService);
    }

    public static IoBoundUseCase ioBoundUseCase() {
        return ioBoundUseCase;
    }

    public static CpuBoundUseCase cpuBoundUseCase() {
        return cpuBoundUseCase;
    }
}
