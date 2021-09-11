import business.external.{MonitoringService, PersistenceRepository, StreamService}
import business.service.{CpuBoundUseCase, IoBoundUseCase}
import com.google.inject.{AbstractModule, Provides}
import common.{KafkaAdapter, MongoDbAdapter, PrometheusAdapter}

class DependencyConfiguration extends AbstractModule {

  override def configure(): Unit = {

    // no need to explicitly bind controllers
    //    bind(classOf[IoController])
    // guice creates an instance eagerly when the application starts
    //      .asEagerSingleton()
    // guice creates an instance from binded class lazily, when needed
    //      .in(Scopes.SINGLETON)
  }

  @Provides
  def kafkaAdapter(): StreamService = {
    new KafkaAdapter()
  }

  @Provides
  def mongoDbAdapter(): PersistenceRepository = {
    new MongoDbAdapter()
  }

  @Provides
  def prometheusAdapter(): MonitoringService = {
    new PrometheusAdapter()
  }

  @Provides
  def cpuUseCase(monitoringService: MonitoringService, streamService: StreamService): CpuBoundUseCase = {
    new CpuBoundUseCase(monitoringService, streamService)
  }

  @Provides
  def ioUseCase(monitoringService: MonitoringService, persistenceRepository: PersistenceRepository): IoBoundUseCase = {
    new IoBoundUseCase(monitoringService, persistenceRepository)
  }
}
