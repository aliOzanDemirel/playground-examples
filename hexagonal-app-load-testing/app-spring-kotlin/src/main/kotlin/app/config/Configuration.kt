package app.config

import business.external.MonitoringService
import business.external.PersistenceRepository
import business.external.StreamService
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import business.common.KafkaAdapter
import business.common.MongoDbAdapter
import business.common.PrometheusAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Configuration {

    @Bean
    fun kafkaAdapter(): StreamService {
        return KafkaAdapter()
    }

    @Bean
    fun mongoDbAdapter(): PersistenceRepository {
        return MongoDbAdapter()
    }

    @Bean
    fun prometheusAdapter(): MonitoringService {
        return PrometheusAdapter()
    }

    @Bean
    fun cpuUseCase(monitoringService: MonitoringService, streamService: StreamService): CpuBoundUseCase {
        return CpuBoundUseCase(monitoringService, streamService)
    }

    @Bean
    fun ioUseCase(monitoringService: MonitoringService, persistenceRepository: PersistenceRepository): IoBoundUseCase {
        return IoBoundUseCase(monitoringService, persistenceRepository)
    }
}