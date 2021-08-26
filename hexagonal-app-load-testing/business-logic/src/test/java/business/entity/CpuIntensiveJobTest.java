package business.entity;

import business.entity.value.CpuWorkInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CpuIntensiveJobTest {

    @Test
    void failWithNullInput() {

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> {
                    CpuIntensiveJob job = new CpuIntensiveJob(null);
                },
                "Work inputs cannot be null or empty!"
        );
    }

    @Test
    void failWithEmptyInput() {

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> {
                    CpuIntensiveJob job = new CpuIntensiveJob(Collections.emptyList());
                },
                "Work inputs cannot be null or empty!"
        );
    }

    @Test
    void checkInvariant() {

        List<CpuWorkInput> inputs = Stream.of("1", "2").map(CpuWorkInput::new).collect(Collectors.toList());
        CpuIntensiveJob job = new CpuIntensiveJob(inputs);
        Assertions.assertNotNull(job);
        Assertions.assertEquals(2, job.getInputCount());
    }
}
