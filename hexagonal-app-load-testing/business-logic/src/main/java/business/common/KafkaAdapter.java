package business.common;

import business.entity.CpuTask;
import business.external.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// normally this adapter should be created in outer layer as application itself will be implementing adapter,
// but for the sake of this example business.common default implementations are created here for all outer layers to use
public class KafkaAdapter implements StreamService {

    private static final Logger log = LoggerFactory.getLogger(KafkaAdapter.class);

    @Override
    public void push(CpuTask cpuTask) {
        log.debug("Streamed cpu task UUID: {}", cpuTask.getId());
    }
}
