package business.service;

import business.Util;
import business.entity.CpuTask;
import business.entity.Metric;
import business.external.MonitoringService;
import business.external.StreamService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
    public void failCpuJobWithFailingMetricCall() {

        var task = new CpuTask.Builder(List.of("1", "2")).build();

        Mockito.doThrow(RuntimeException.class).when(monitoringService).push(any());
        var result = cpuBoundUseCase.compute(task);

        Assertions.assertNull(result);
    }

    @Test
    public void runCpuJob_checkSentMetric() {

        var task = new CpuTask.Builder(List.of("1", "2", "3")).build();

        var result = cpuBoundUseCase.compute(task);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.size());

        ArgumentCaptor<Metric> captor = ArgumentCaptor.forClass(Metric.class);
        Mockito.verify(monitoringService).push(captor.capture());
        Metric capturedParameter = captor.getValue();
        Assertions.assertEquals(Metric.Category.CPU, capturedParameter.getCategory());
        Assertions.assertEquals("cpu_work_duration", capturedParameter.getMetricName());

        Mockito.verify(streamService, Mockito.times(1)).push(any());
    }

    @Test
    public void runCpuJob_withHugeInput() {

        String param = Util.prepareHugeString(100_000);
        List<String> params = List.of(param, param.substring(param.length() / 3));
        var task = new CpuTask.Builder(params).build();

        var result = cpuBoundUseCase.compute(task);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());

        Mockito.verify(streamService, Mockito.times(1)).push(any());
        Mockito.verify(monitoringService, Mockito.times(1)).push(any());
    }
}