package business.entity;

import business.entity.value.CpuWorkInput;

import java.util.ArrayList;
import java.util.List;

// very simple domain object example, so there is no behaviour encapsulated because of simplicity of object
public class CpuIntensiveJob {

    // invariants of domain entities and aggregate roots must always hold!
    // there should always be validation of internal state if there is external modification
    public CpuIntensiveJob(List<CpuWorkInput> input) {

        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Work inputs cannot be null or empty!");
        }
        workInputs = new ArrayList<>(input);
        // very simple example of holding invariant of object
        inputCount = workInputs.size();
    }

    private final List<CpuWorkInput> workInputs;
    private final int inputCount;

    public List<CpuWorkInput> getWorkInputs() {
        return workInputs;
    }

    public int getInputCount() {
        return inputCount;
    }

}
