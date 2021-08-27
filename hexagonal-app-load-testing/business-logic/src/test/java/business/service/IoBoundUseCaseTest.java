package business.service;

import business.entity.IoTask;
import business.entity.Metric;
import business.external.MonitoringService;
import business.external.PersistenceRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;

@ExtendWith(MockitoExtension.class)
public class IoBoundUseCaseTest {

    @Mock
    private PersistenceRepository persistenceRepository;
    @Mock
    private MonitoringService monitoringService;
    @InjectMocks
    private IoBoundUseCase ioBoundUseCase;

    @Test
    public void ioTaskDefaultBehaviourBlocks_atLeastGivenDuration() {

        long duration = 2000;
        IoTask<Boolean> ioTask = new IoTask.IoTaskBuilder<>(UUID.randomUUID(), IoTask.defaultBlockingBehaviour())
                .duration(duration)
                .build();

        long start = System.currentTimeMillis();
        Boolean result = ioBoundUseCase.run(ioTask);
        long end = System.currentTimeMillis();
        Assertions.assertTrue(result);

        boolean tookMoreThanDuration = end - start > duration;
        Assertions.assertTrue(tookMoreThanDuration);

        Mockito.verify(persistenceRepository, Mockito.times(1)).persist(same(ioTask));
        Mockito.verify(monitoringService, Mockito.times(1)).push(any(Metric.class));
    }
}
