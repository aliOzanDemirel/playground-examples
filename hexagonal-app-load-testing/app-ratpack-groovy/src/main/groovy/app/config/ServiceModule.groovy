package app.config

import app.api.CpuRequestHandler
import app.api.IoRequestHandler
import app.service.AppRatpackService
import business.external.MonitoringService
import business.external.PersistenceRepository
import business.external.StreamService
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Scopes
import common.KafkaAdapter
import common.MongoDbAdapter
import common.PrometheusAdapter

class ServiceModule extends AbstractModule {

    /**
     * dependency injection configuration, much like creating spring beans
     */
    @Override
    protected void configure() {

        bind(AppRatpackService.class).asEagerSingleton()
        bind(CpuRequestHandler.class).in(Scopes.SINGLETON)
        bind(IoRequestHandler.class).in(Scopes.SINGLETON)
    }

    @Provides
    StreamService kafkaAdapter() {
        new KafkaAdapter()
    }

    @Provides
    PersistenceRepository mongoDbAdapter() {
        new MongoDbAdapter()
    }

    @Provides
    MonitoringService prometheusAdapter() {
        new PrometheusAdapter()
    }

    @Provides
    CpuBoundUseCase cpuUseCase(MonitoringService monitoringService, StreamService streamService) {
        new CpuBoundUseCase(monitoringService, streamService)
    }

    @Provides
    IoBoundUseCase ioUseCase(MonitoringService monitoringService, PersistenceRepository persistenceRepository) {
        new IoBoundUseCase(monitoringService, persistenceRepository)
    }
}