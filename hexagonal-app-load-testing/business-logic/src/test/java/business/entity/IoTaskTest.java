package business.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class IoTaskTest {

    @Test
    void failWithNoId() {

        Assertions.assertThrows(IllegalStateException.class,
                () -> {
                    IoTask task = new IoTask.IoTaskBuilder(null).build();
                },
                "Cannot build BlockingTask as internal state is invalid!"
        );
    }

    @Test
    void failWithInvalidDuration() {

        Assertions.assertThrows(IllegalStateException.class,
                () -> {
                    IoTask task = new IoTask.IoTaskBuilder(UUID.randomUUID()).duration(50).build();
                },
                "Cannot build BlockingTask as internal state is invalid!"
        );
    }

    @Test
    void checkConstructedMetric() {

        IoTask task = new IoTask.IoTaskBuilder(UUID.randomUUID()).duration(1000).build();
        Assertions.assertNotNull(task);

        Metric metric = task.getMetric();
        Assertions.assertNotNull(metric);
        Assertions.assertEquals(Metric.Category.BLOCKING, metric.getCategory());
        Assertions.assertEquals("1000", metric.getValue());
    }
}
