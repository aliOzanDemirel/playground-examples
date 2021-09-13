package app.config;

import business.external.MonitoringService;
import business.external.PersistenceRepository;
import business.external.StreamService;
import business.service.CpuBoundUseCase;
import business.service.IoBoundUseCase;
import business.common.KafkaAdapter;
import business.common.MongoDbAdapter;
import business.common.PrometheusAdapter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class DependencyBinding extends AbstractBinder {

    @Override
    protected void configure() {

        // business.common module dependencies injected manually
        var mongoDbAdapter = new MongoDbAdapter();
        bind(mongoDbAdapter).to(PersistenceRepository.class);

        var kafkaAdapter = new KafkaAdapter();
        bind(kafkaAdapter).to(StreamService.class);

        var prometheusAdapter = new PrometheusAdapter();
        bind(prometheusAdapter).to(MonitoringService.class);

        var ioBoundUseCase = new IoBoundUseCase(prometheusAdapter, mongoDbAdapter);
        bind(ioBoundUseCase).to(IoBoundUseCase.class);

        var cpuBoundUseCase = new CpuBoundUseCase(prometheusAdapter, kafkaAdapter);
        bind(cpuBoundUseCase).to(CpuBoundUseCase.class);
    }
}
