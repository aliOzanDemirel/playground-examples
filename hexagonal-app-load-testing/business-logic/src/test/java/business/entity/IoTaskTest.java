package business.entity;

import business.error.InvariantViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static business.entity.IoTask.defaultBlockingBehaviour;

public class IoTaskTest {

    @Test
    void failWithNoId() {

        Assertions.assertThrows(InvariantViolationException.class,
                () -> {
                    IoTask<Boolean> task = new IoTask.Builder<>(null, defaultBlockingBehaviour()).build();
                },
                "ID of io task is not provided!"
        );
    }

    @Test
    void failWithNoBehaviour() {

        Assertions.assertThrows(InvariantViolationException.class,
                () -> {
                    IoTask<Object> task = new IoTask.Builder<>(UUID.randomUUID(), null).build();
                },
                "Behaviour of io task is not defined!"
        );
    }

    @Test
    void failWithInvalidDuration() {

        Assertions.assertThrows(InvariantViolationException.class,
                () -> {
                    IoTask<Boolean> task = new IoTask.Builder<>(UUID.randomUUID(), defaultBlockingBehaviour()).duration(50).build();
                },
                "Duration should be between 1000 and 120.000!"
        );
    }

    @Test
    void checkConstructedMetric() {

        var id = UUID.randomUUID();
        var task = new IoTask.Builder<>(id, defaultBlockingBehaviour()).duration(1000).build();
        Assertions.assertNotNull(task);
        Assertions.assertEquals(id, task.getId());

        Metric metric = task.getMetric();
        Assertions.assertNotNull(metric);
        Assertions.assertEquals(Metric.Category.IO, metric.getCategory());
        Assertions.assertEquals("1000", metric.getValue());
        Assertions.assertEquals("io_duration_ms", metric.getMetricName());
    }
}
