package app

import app.api.RequestHandler
import app.config.javalinAppConfigurer
import app.service.AppJavalinService
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import business.common.KafkaAdapter
import business.common.MongoDbAdapter
import business.common.PrometheusAdapter
import io.javalin.Javalin
import org.eclipse.jetty.util.log.Log
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() {

    // setup DI context
    startKoin { modules(businessModule, appModule) }

    val app = Javalin.create(javalinAppConfigurer).start(8080)

    val server = app.config.inner.server
    val pool = server!!.threadPool
    if (pool is QueuedThreadPool) {
        Log.getLog().info("Server is configured with maxThreads: ${pool.maxThreads} and minThreads: ${pool.minThreads}")
    }

    app.get("/io", RequestHandler.io)
    app.post("/cpu", RequestHandler.cpu)
}

val businessModule = module {

    single { KafkaAdapter() }
    single { MongoDbAdapter() }
    single { PrometheusAdapter() }
    single { CpuBoundUseCase(get(PrometheusAdapter::class), get(KafkaAdapter::class)) }
    single { IoBoundUseCase(get(PrometheusAdapter::class), get(MongoDbAdapter::class)) }
}

val appModule = module {

    single { AppJavalinService(get(IoBoundUseCase::class), get(CpuBoundUseCase::class)) }
}