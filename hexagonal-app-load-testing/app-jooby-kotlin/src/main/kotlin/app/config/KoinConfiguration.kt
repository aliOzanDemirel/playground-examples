package app.config

import app.service.AppJoobyService
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import business.common.KafkaAdapter
import business.common.MongoDbAdapter
import business.common.PrometheusAdapter
import org.koin.dsl.module

val businessModule = module {

    single { KafkaAdapter() }
    single { MongoDbAdapter() }
    single { PrometheusAdapter() }
    single { CpuBoundUseCase(get(PrometheusAdapter::class), get(KafkaAdapter::class)) }
    single { IoBoundUseCase(get(PrometheusAdapter::class), get(MongoDbAdapter::class)) }
}

val appModule = module {

    single { AppJoobyService(get(IoBoundUseCase::class), get(CpuBoundUseCase::class)) }
}