package business.service;

import business.entity.Metric;
import business.external.MonitoringService;
import business.external.StreamService;
import business.service.dto.WorkCpuCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class CpuBoundUseCaseTest {

    @Mock
    private StreamService streamService;
    @Mock
    private MonitoringService monitoringService;
    @InjectMocks
    private CpuBoundUseCase cpuBoundUseCase;

    @Test
    public void failCpuJobWithWrongInput() {

        WorkCpuCommand command = new WorkCpuCommand(List.of("1", ""));
        Long result = cpuBoundUseCase.workCpu(command);
        Assertions.assertNull(result);
    }

    @Test
    public void failCpuJobWithFailingMetricCall() {

        WorkCpuCommand command = new WorkCpuCommand(List.of("1", "2"));
        Mockito.doThrow(RuntimeException.class).when(monitoringService).push(any());
        Long result = cpuBoundUseCase.workCpu(command);
        Assertions.assertNull(result);
    }

    @Test
    public void runCpuJob_checkSentMetric() {

        WorkCpuCommand command = new WorkCpuCommand(List.of("1", "2", "3"));
        Long result = cpuBoundUseCase.workCpu(command);
        Assertions.assertNotNull(result);

        ArgumentCaptor<Metric> captor = ArgumentCaptor.forClass(Metric.class);
        Mockito.verify(monitoringService).push(captor.capture());
        Metric capturedParameter = captor.getValue();
        Assertions.assertEquals(Metric.Category.CPU, capturedParameter.getCategory());
        Assertions.assertEquals("cpu_work_duration", capturedParameter.getMetricName());

        Mockito.verify(streamService, Mockito.times(1)).push(any());
    }

    @Test
    public void runCpuJob_withHugeInput() {

        String param = prepareHugeString(100_000);
        List<String> params = List.of(param, param.substring(param.length() / 3));
        WorkCpuCommand command = new WorkCpuCommand(params);

        Long result = cpuBoundUseCase.workCpu(command);
        Assertions.assertNotNull(result);
        Mockito.verify(streamService, Mockito.times(1)).push(any());
        Mockito.verify(monitoringService, Mockito.times(1)).push(any());
    }

    @Test
    public void failCpuJob_inputIsTooBig() {

        String param = prepareHugeString(200_000);
        WorkCpuCommand command = new WorkCpuCommand(List.of(param));

        Long result = cpuBoundUseCase.workCpu(command);
        Assertions.assertNull(result);
        Mockito.verify(streamService, Mockito.never()).push(any());
        Mockito.verify(monitoringService, Mockito.never()).push(any());
    }

    private String prepareHugeString(int bound) {

        String param = UUID.randomUUID().toString();
        while (true) {
            String temp = param + UUID.randomUUID();
            if (temp.length() < bound) {
                param = temp;
            } else {
                break;
            }
        }
        return param;
    }
}
