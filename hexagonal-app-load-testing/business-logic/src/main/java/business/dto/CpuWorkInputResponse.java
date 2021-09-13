package business.dto;

import java.util.Objects;

public class CpuWorkInputResponse {

    public CpuWorkInputResponse(long inputProcessingDuration, int processedInputLength, boolean searchSuccess, int inputIndex) {
        this.inputProcessingDuration = inputProcessingDuration;
        this.processedInputLength = processedInputLength;
        this.searchSuccess = searchSuccess;
        this.inputIndex = inputIndex;
    }

    // in nanoseconds
    public long inputProcessingDuration;
    public int processedInputLength;
    public boolean searchSuccess;
    public int inputIndex;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CpuWorkInputResponse that = (CpuWorkInputResponse) o;
        return inputIndex == that.inputIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputIndex);
    }
}
