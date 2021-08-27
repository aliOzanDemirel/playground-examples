package business.external;

import business.entity.CpuTask;
import business.port.ExternalAdapterApi;

@ExternalAdapterApi
public interface StreamService {

    void push(CpuTask cpuTask);
}
