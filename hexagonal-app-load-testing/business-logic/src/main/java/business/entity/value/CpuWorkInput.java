package business.entity.value;

import business.error.InvariantViolationException;

// basically a value object that enforces an invariant, rather than representing same data through primitive variables
public class CpuWorkInput {

    public CpuWorkInput(String inputData) {

        if (inputData == null || inputData.isBlank() || inputData.length() > 100_000) {
            throw new InvariantViolationException("Data of work input is invalid!");
        }
        this.inputData = inputData;
    }

    private final String inputData;

    public String getInputData() {
        return inputData;
    }
}
