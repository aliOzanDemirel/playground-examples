package business.entity;

import business.Util;
import business.error.InvariantViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CpuTaskTest {

    @Test
    void failWithNoId() {

        Assertions.assertThrows(InvariantViolationException.class,
                () -> {
                    var task = new CpuTask.Builder(null, List.of("1")).build();
                },
                "ID of cpu task is not provided!"
        );
    }

    @Test
    void failWithNullInput() {

        Assertions.assertThrows(InvariantViolationException.class,
                () -> {
                    var task = new CpuTask.Builder(null).build();
                },
                "Work input for cpu task is not provided!"
        );
    }

    @Test
    void failWithEmptyInput() {

        Assertions.assertThrows(InvariantViolationException.class,
                () -> {
                    var task = new CpuTask.Builder(Collections.emptyList()).build();
                },
                "Work input for cpu task is not provided!"
        );
    }

    @Test
    void failWithEmptyInputData() {

        Assertions.assertThrows(InvariantViolationException.class,
                () -> {
                    var task = new CpuTask.Builder(List.of("1", "")).build();
                },
                "Data of work input is invalid!"
        );
    }

    @Test
    void failWithTooBigInputData() {

        Assertions.assertThrows(InvariantViolationException.class,
                () -> {
                    String param = Util.prepareHugeString(200_000);
                    var task = new CpuTask.Builder(List.of(param)).build();
                },
                "Data of work input is invalid!"
        );
    }

    @Test
    void checkInvariant() {

        var task = new CpuTask.Builder(Stream.of("1", "2").collect(Collectors.toList())).build();
        Assertions.assertNotNull(task);
        Assertions.assertNotNull(task.getId());
        Assertions.assertNotSame(task.getWorkInputs(), task.getWorkInputs());

        Assertions.assertEquals(2, task.getWorkInputs().size());
        Assertions.assertEquals(2, task.getInputCount());
    }
}
