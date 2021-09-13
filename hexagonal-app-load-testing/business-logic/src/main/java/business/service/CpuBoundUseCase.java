package business.service;

import business.dto.CpuWorkInputResponse;
import business.entity.CpuTask;
import business.external.MonitoringService;
import business.external.StreamService;
import business.port.BusinessInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// not made implementation of actual interface for sake of simplicity, so this concrete class itself is public API to caller
@BusinessInterface
public class CpuBoundUseCase {

    private static final Logger log = LoggerFactory.getLogger(CpuBoundUseCase.class);

    private final MonitoringService monitoringService;
    private final StreamService streamService;

    public CpuBoundUseCase(MonitoringService monitoringService, StreamService streamService) {
        this.monitoringService = monitoringService;
        this.streamService = streamService;
    }

    // execute compute logic to use CPU and keep it busy
    public Set<CpuWorkInputResponse> compute(CpuTask task) {

        try {
            long start = System.nanoTime();

            Set<CpuWorkInputResponse> allInputResponses = IntStream.rangeClosed(0, task.getInputCount() - 1)
                    .mapToObj(task::run)
                    .collect(Collectors.toSet());

            long duration = System.nanoTime() - start;
            log.info("Compute work for all inputs took {} milliseconds", duration / 1_000_000);

            monitoringService.push(task.getMetric(duration));
            streamService.push(task);
            return allInputResponses;
        } catch (Exception e) {

            log.error("Error caught in use case!", e);
            return Collections.emptySet();
        }
    }
}