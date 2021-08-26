package app.config

import business.external.MonitoringService
import business.external.PersistenceRepository
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import common.MonitoringServiceAdapter
import common.PersistenceRepositoryAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Configuration {

    @Bean
    fun persistentRepository(): PersistenceRepository {
        return PersistenceRepositoryAdapter()
    }

    @Bean
    fun monitoringService(): MonitoringService {
        return MonitoringServiceAdapter()
    }

    @Bean
    fun cpuUseCase(monitoringService: MonitoringService): CpuBoundUseCase {
        return CpuBoundUseCase(monitoringService)
    }

    @Bean
    fun ioUseCase(monitoringService: MonitoringService, persistenceRepository: PersistenceRepository): IoBoundUseCase {
        return IoBoundUseCase(monitoringService, persistenceRepository)
    }
}