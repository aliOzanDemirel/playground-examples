package app.config

import app.api.CpuRequestHandler
import app.api.IoRequestHandler
import app.service.BusinessLogicClient
import business.external.MonitoringService
import business.external.PersistenceRepository
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Scopes
import common.MonitoringServiceAdapter
import common.PersistenceRepositoryAdapter

class ServiceModule extends AbstractModule {

    /**
     * dependency injection configuration, much like creating spring beans
     */
    @Override
    protected void configure() {

        bind(BusinessLogicClient.class).asEagerSingleton()
        bind(CpuRequestHandler.class).in(Scopes.SINGLETON)
        bind(IoRequestHandler.class).in(Scopes.SINGLETON)
    }

    @Provides
    PersistenceRepository persistentRepository() {
        new PersistenceRepositoryAdapter()
    }

    @Provides
    MonitoringService monitoringService() {
        new MonitoringServiceAdapter()
    }

    @Provides
    CpuBoundUseCase cpuUseCase(MonitoringService monitoringService) {
        new CpuBoundUseCase(monitoringService)
    }

    @Provides
    IoBoundUseCase ioUseCase(MonitoringService monitoringService, PersistenceRepository persistenceRepository) {
        new IoBoundUseCase(monitoringService, persistenceRepository)
    }
}