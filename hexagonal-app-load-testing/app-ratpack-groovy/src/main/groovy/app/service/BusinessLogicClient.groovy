package app.service

import app.api.dto.WorkCpuResponse
import business.entity.IoTask
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import business.service.dto.WorkCpuCommand
import groovy.util.logging.Slf4j
import ratpack.exec.Blocking
import ratpack.exec.Promise

import javax.inject.Inject

@Slf4j
class BusinessLogicClient {

    private IoBoundUseCase ioBoundUseCase
    private CpuBoundUseCase cpuBoundUseCase

    @Inject
    BusinessLogicClient(IoBoundUseCase ioBoundUseCase, CpuBoundUseCase cpuBoundUseCase) {
        this.ioBoundUseCase = ioBoundUseCase
        this.cpuBoundUseCase = cpuBoundUseCase
    }

    Promise<WorkCpuResponse> workCpu(List<String> inputs) {

        log.debug("Computing with {} inputs", inputs.size())

        def command = new WorkCpuCommand(inputs)
        def durationInNanos = cpuBoundUseCase.workCpu(command)
        return Promise.value(new WorkCpuResponse(durationInNanos))
    }

    Promise<Boolean> blockingIo(int duration) {

        log.debug("IO with simulated duration {}", duration)

        UUID id = UUID.randomUUID()
        IoTask task = new IoTask.IoTaskBuilder(id).duration(duration).build()
        Blocking.get {
            ioBoundUseCase.block(task)
        }
    }
}
