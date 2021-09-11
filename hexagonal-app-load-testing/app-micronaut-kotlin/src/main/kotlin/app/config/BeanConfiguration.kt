package app.config

import business.external.MonitoringService
import business.external.PersistenceRepository
import business.external.StreamService
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import common.KafkaAdapter
import common.MongoDbAdapter
import common.PrometheusAdapter
import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton

@Factory
class BeanConfiguration {

    @Singleton
    @Named("kafka")
    fun kafkaAdapter(): StreamService {
        return KafkaAdapter()
    }

    @Singleton
    @Named("mongo")
    fun mongoDbAdapter(): PersistenceRepository {
        return MongoDbAdapter()
    }

    @Singleton
    @Named("prometheus")
    fun prometheusAdapter(): MonitoringService {
        return PrometheusAdapter()
    }

    @Singleton
    fun cpuUseCase(monitoringService: MonitoringService, streamService: StreamService): CpuBoundUseCase {
        return CpuBoundUseCase(monitoringService, streamService)
    }

    @Singleton
    fun ioUseCase(monitoringService: MonitoringService, persistenceRepository: PersistenceRepository): IoBoundUseCase {
        return IoBoundUseCase(monitoringService, persistenceRepository)
    }
}