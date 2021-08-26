package business.external;

import business.entity.Metric;
import business.port.ExternalAdapterApi;

@ExternalAdapterApi
public interface MonitoringService {

    void push(Metric metric);
}
