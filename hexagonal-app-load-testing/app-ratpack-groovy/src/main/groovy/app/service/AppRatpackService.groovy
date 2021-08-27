package app.service

import app.dto.WorkCpuResponse
import business.entity.IoTask
import business.service.CpuBoundUseCase
import business.service.IoBoundUseCase
import business.service.dto.WorkCpuCommand
import groovy.util.logging.Slf4j
import ratpack.exec.Blocking
import ratpack.exec.Promise

import javax.inject.Inject

@Slf4j
class AppRatpackService {

    private IoBoundUseCase ioBoundUseCase
    private CpuBoundUseCase cpuBoundUseCase

    @Inject
    AppRatpackService(IoBoundUseCase ioBoundUseCase, CpuBoundUseCase cpuBoundUseCase) {
        this.ioBoundUseCase = ioBoundUseCase
        this.cpuBoundUseCase = cpuBoundUseCase
    }

    Promise<WorkCpuResponse> compute(List<String> inputs) {

        log.debug("CPU task with {} inputs", inputs.size())

        // promise is lazy, will be resolved later once some thread subscribes to it
        Promise.sync {
            def command = new WorkCpuCommand(inputs)
            def durationInNanos = cpuBoundUseCase.workCpu(command)
            new WorkCpuResponse(durationInNanos)
        }
    }

    Promise<Boolean> io(long duration) {

        log.debug("IO task with delegated blocking thread, duration {}", duration)

        UUID id = UUID.randomUUID()
        IoTask<Boolean> task = new IoTask.IoTaskBuilder<Boolean>(id, IoTask.defaultBlockingBehaviour()).duration(duration).build()

        // very simple to delegate blocking call in ratpack, because of its execution flow
        // it is important to not keep this separate blocking thread busy with computation
        Blocking.get {
            ioBoundUseCase.run(task)
        }
    }
}
