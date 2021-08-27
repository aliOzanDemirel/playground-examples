package business.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static business.entity.IoTask.defaultBlockingBehaviour;

public class IoTaskTest {

    @Test
    void failWithNoId() {

        Assertions.assertThrows(IllegalStateException.class,
                () -> {
                    IoTask<Boolean> task = new IoTask.IoTaskBuilder<>(null, defaultBlockingBehaviour()).build();
                },
                "Cannot build BlockingTask as internal state is invalid!"
        );
    }

    @Test
    void failWithNoBehaviour() {

        Assertions.assertThrows(IllegalStateException.class,
                () -> {
                    IoTask<Object> task = new IoTask.IoTaskBuilder<>(UUID.randomUUID(), null).build();
                },
                "Cannot build BlockingTask as internal state is invalid!"
        );
    }

    @Test
    void failWithInvalidDuration() {

        Assertions.assertThrows(IllegalStateException.class,
                () -> {
                    IoTask<Boolean> task = new IoTask.IoTaskBuilder<>(UUID.randomUUID(), defaultBlockingBehaviour()).duration(50).build();
                },
                "Cannot build BlockingTask as internal state is invalid!"
        );
    }

    @Test
    void checkConstructedMetric() {

        IoTask<Boolean> task = new IoTask.IoTaskBuilder<>(UUID.randomUUID(), defaultBlockingBehaviour()).duration(1000).build();
        Assertions.assertNotNull(task);

        Metric metric = task.getMetric();
        Assertions.assertNotNull(metric);
        Assertions.assertEquals(Metric.Category.IO, metric.getCategory());
        Assertions.assertEquals("1000", metric.getValue());
        Assertions.assertEquals("io_duration_ms", metric.getMetricName());
    }
}
