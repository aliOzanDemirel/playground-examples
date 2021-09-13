package app.config

import business.external.MonitoringService
import business.external.PersistenceRepository
import business.external.StreamService
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import business.common.KafkaAdapter
import business.common.MongoDbAdapter
import business.common.PrometheusAdapter
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces

@ApplicationScoped
class BeanConfiguration {

    @Produces
    fun kafkaAdapter(): StreamService {
        return KafkaAdapter()
    }

    @Produces
    fun mongoDbAdapter(): PersistenceRepository {
        return MongoDbAdapter()
    }

    @Produces
    fun prometheusAdapter(): MonitoringService {
        return PrometheusAdapter()
    }

    @Produces
    fun cpuUseCase(monitoringService: MonitoringService, streamService: StreamService): CpuBoundUseCase {
        return CpuBoundUseCase(monitoringService, streamService)
    }

    @Produces
    fun ioUseCase(monitoringService: MonitoringService, persistenceRepository: PersistenceRepository): IoBoundUseCase {
        return IoBoundUseCase(monitoringService, persistenceRepository)
    }
}