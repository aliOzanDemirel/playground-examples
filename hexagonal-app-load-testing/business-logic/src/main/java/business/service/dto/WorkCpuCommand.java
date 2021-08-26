package business.service.dto;

import java.util.List;

// cant make record as project language level is 11
public class WorkCpuCommand {

    private final List<String> inputs;

    public WorkCpuCommand(List<String> inputs) {
        this.inputs = inputs;
    }

    public List<String> getInputs() {
        return inputs;
    }
}
