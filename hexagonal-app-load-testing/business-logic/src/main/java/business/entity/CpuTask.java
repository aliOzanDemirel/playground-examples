package business.entity;

import business.dto.CpuWorkInputResponse;
import business.entity.value.CpuWorkInput;
import business.error.InvariantViolationException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// very simple domain object example, so there is no behaviour encapsulated because of simplicity of object
public class CpuTask {

    private static final int DEFAULT_CONCAT_ITERATION_COUNT = 200;

    public CpuTask(Builder builder) {

        id = builder.id;
        concatIterationCount = builder.concatIterationCount;
        workInputs = builder.workInputs;

        // very basic example of holding invariant of object, no outside force can modify this
        // if the work inputs somehow can be modified, then this count should have been updated as well
        inputCount = workInputs.size();
    }

    private final UUID id;
    private final List<CpuWorkInput> workInputs;
    private final int inputCount;
    private final int concatIterationCount;

    public UUID getId() {
        return id;
    }

    public List<CpuWorkInput> getWorkInputs() {
        return List.copyOf(workInputs);
    }

    public int getInputCount() {
        return inputCount;
    }

    public Metric getMetric(long duration) {
        return new Metric(Metric.Category.CPU, "cpu_work_duration", String.valueOf(duration));
    }

    // internal logic of cpu task
    public CpuWorkInputResponse run(int inputIndex) {

        if (inputIndex < 0 || inputIndex >= inputCount) {
            throw new RuntimeException("Invalid input index provided -> " + inputIndex);
        }

        long start = System.nanoTime();

        String rawInputData = workInputs.get(inputIndex).getInputData();
        StringBuilder concatInIteration = new StringBuilder();
        for (int i = 0; i < concatIterationCount; i++) {
            concatInIteration
                    .append(rawInputData)
                    .append(LocalDateTime.now());
        }

        String finalString = concatInIteration.toString();
        int utf8Length = finalString.getBytes(StandardCharsets.UTF_8).length;
        boolean searchSuccess = finalString.contains("YOU_CANT_FIND_ME");
        long duration = System.nanoTime() - start;

        return new CpuWorkInputResponse(duration, utf8Length, searchSuccess, inputIndex);
    }

    @Override
    public String toString() {
        return "CpuTask - id: " + id + ", inputCount: " + inputCount;
    }

    public static class Builder {

        // some identifier
        private final UUID id;

        private List<CpuWorkInput> workInputs;
        private int concatIterationCount;

        public Builder(List<String> workInputs) {

            this(UUID.randomUUID(), workInputs);
        }

        public Builder(UUID id, List<String> workInputs) {

            this.id = id;
            this.concatIterationCount = DEFAULT_CONCAT_ITERATION_COUNT;

            if (workInputs != null) {
                this.workInputs = workInputs.stream().map(CpuWorkInput::new).collect(Collectors.toList());
            }
        }

        public Builder iterationCount(int iterationCount) {
            this.concatIterationCount = iterationCount;
            return this;
        }

        // invariants of domain entities and aggregate roots must always hold! there should always be validation of
        // internal state if there is external modification, for example a setter to some entity property should
        // always enforce the invariant rule validation just like this builder does
        public CpuTask build() {

            if (id == null) {
                throw new InvariantViolationException("ID of cpu task is not provided!");
            }
            if (workInputs == null || workInputs.isEmpty()) {
                throw new InvariantViolationException("Work input for cpu task is not provided!");
            }
            return new CpuTask(this);
        }
    }
}
