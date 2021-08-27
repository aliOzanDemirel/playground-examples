package business.service;

import business.entity.CpuTask;
import business.entity.value.CpuWorkInput;
import business.external.MonitoringService;
import business.external.StreamService;
import business.port.BusinessInterface;
import business.service.dto.WorkCpuCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// not made implementation of actual interface for sake of simplicity, so this concrete class itself is public API to caller
@BusinessInterface
public class CpuBoundUseCase {

    private static final Logger log = LoggerFactory.getLogger(CpuBoundUseCase.class);
    private static final int CONCAT_ITERATION_COUNT = 300;

    private final MonitoringService monitoringService;
    private final StreamService streamService;

    public CpuBoundUseCase(MonitoringService monitoringService, StreamService streamService) {
        this.monitoringService = monitoringService;
        this.streamService = streamService;
    }

    public Long workCpu(WorkCpuCommand command) {

        try {
            CpuTask cpuTask = constructDomainObject(command);
            long duration = workOnInputs(cpuTask);

            monitoringService.push(cpuTask.getMetric(duration));
            streamService.push(cpuTask);
            return duration;
        } catch (Exception e) {

            log.error("Error caught in use case!", e);
            return null;
        }
    }

    // execute compute logic to use CPU and keep it busy
    private long workOnInputs(CpuTask cpuTask) {

        long start = System.nanoTime();

        String allInputsCombined = cpuTask.getWorkInputs().stream().map(CpuWorkInput::getInputData).collect(Collectors.joining());
        StringBuilder concatInIteration = new StringBuilder();
        for (int i = 0; i < CONCAT_ITERATION_COUNT; i++) {
            concatInIteration
                    .append(allInputsCombined)
                    .append(LocalDateTime.now());
        }

        String finalString = concatInIteration.toString();
        int utf8Length = finalString.getBytes(StandardCharsets.UTF_8).length;
        boolean searchSuccess = finalString.contains("YOU_CANT_FIND_ME");

        long duration = System.nanoTime() - start;
        log.info("Joined {} inputs and concatenated {} times in {} nanoseconds, utf-8 length is {}, search token found: {}",
                cpuTask.getInputCount(), CONCAT_ITERATION_COUNT, duration, utf8Length, searchSuccess);
        return duration;
    }

    private CpuTask constructDomainObject(WorkCpuCommand command) {

        List<CpuWorkInput> inputs = command.getInputs().stream().map(CpuWorkInput::new).collect(Collectors.toList());
        return new CpuTask(inputs);
    }
}
