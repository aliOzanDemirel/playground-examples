package app

import business.external.MonitoringService
import business.external.PersistenceRepository
import business.external.StreamService
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import common.KafkaAdapter
import common.MongoDbAdapter
import common.PrometheusAdapter

object DependencyFactory {

    private val persistenceRepository: PersistenceRepository
    private val streamService: StreamService
    private val monitoringService: MonitoringService
    private val ioBoundUseCase: IoBoundUseCase
    private val cpuBoundUseCase: CpuBoundUseCase

    init {
        persistenceRepository = MongoDbAdapter()
        streamService = KafkaAdapter()
        monitoringService = PrometheusAdapter()
        ioBoundUseCase = IoBoundUseCase(monitoringService, persistenceRepository)
        cpuBoundUseCase = CpuBoundUseCase(monitoringService, streamService)
    }

    fun ioBoundUseCase(): IoBoundUseCase {
        return ioBoundUseCase
    }

    fun cpuBoundUseCase(): CpuBoundUseCase {
        return cpuBoundUseCase
    }
}