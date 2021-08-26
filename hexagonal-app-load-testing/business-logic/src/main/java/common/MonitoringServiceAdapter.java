package common;

import business.entity.Metric;
import business.external.MonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// normally this adapter should lie in outer layer code as application itself will be implementing adapter,
// but for the sake of this example common implementations are created here for all outer layers to use
public class MonitoringServiceAdapter implements MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringServiceAdapter.class);

    @Override
    public void push(Metric metric) {
        log.debug("Sent metric for {}", metric.getCategory());
    }
}
