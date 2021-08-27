package business.entity;

import business.entity.value.CpuWorkInput;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// very simple domain object example, so there is no behaviour encapsulated because of simplicity of object
public class CpuTask {

    // invariants of domain entities and aggregate roots must always hold!
    // there should always be validation of internal state if there is external modification
    public CpuTask(List<CpuWorkInput> input) {

        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Work inputs cannot be null or empty!");
        }
        workInputs = new ArrayList<>(input);
        // very simple example of holding invariant of object
        inputCount = workInputs.size();
        id = UUID.randomUUID();
    }

    private final UUID id;
    private final List<CpuWorkInput> workInputs;
    private final int inputCount;

    public UUID getId() {
        return id;
    }

    public List<CpuWorkInput> getWorkInputs() {
        return workInputs;
    }

    public int getInputCount() {
        return inputCount;
    }

    public Metric getMetric(long duration) {
        return new Metric(Metric.Category.CPU, "cpu_work_duration", String.valueOf(duration));
    }
}
