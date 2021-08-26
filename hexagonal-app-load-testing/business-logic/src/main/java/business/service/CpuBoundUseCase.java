package business.service;

import business.entity.CpuIntensiveJob;
import business.entity.Metric;
import business.entity.value.CpuWorkInput;
import business.external.MonitoringService;
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

    public CpuBoundUseCase(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    public Long workCpu(WorkCpuCommand command) {

        try {
            CpuIntensiveJob job = constructDomainObject(command);
            long duration = compute(job);
            sendMetric(duration);
            return duration;

        } catch (Exception e) {
            log.error("Error occurred while executing cpu intensive job!", e);
            return null;
        }
    }

    // execute compute logic to use CPU and keep it busy
    private long compute(CpuIntensiveJob job) {

        long start = System.nanoTime();

        String allInputsCombined = job.getWorkInputs().stream().map(CpuWorkInput::getInputData).collect(Collectors.joining());
        StringBuilder concatInIteration = new StringBuilder();
        for (int i = 0; i < CONCAT_ITERATION_COUNT; i++) {
            concatInIteration.append(allInputsCombined).append(LocalDateTime.now());
        }

        int utf8Length = concatInIteration.toString().getBytes(StandardCharsets.UTF_8).length;
        log.debug("Written {} inputs and concatenated {} times, total length with UTF8 formatting: {}",
                job.getInputCount(), CONCAT_ITERATION_COUNT, utf8Length);

        return System.nanoTime() - start;
    }

    private void sendMetric(long duration) {

        Metric metric = new Metric(Metric.Category.CPU_WORK, "cpu_work_duration", String.valueOf(duration));
        monitoringService.push(metric);
    }

    private CpuIntensiveJob constructDomainObject(WorkCpuCommand command) {

        List<CpuWorkInput> inputs = command.getInputs().stream().map(CpuWorkInput::new).collect(Collectors.toList());
        return new CpuIntensiveJob(inputs);
    }
}
