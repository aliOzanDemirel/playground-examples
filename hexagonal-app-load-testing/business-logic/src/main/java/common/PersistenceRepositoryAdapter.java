package common;

import business.entity.IoTask;
import business.external.PersistenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// normally this adapter should be created in outer layer as application itself will be implementing adapter,
// but for the sake of this example common default implementations are created here for all outer layers to use
public class PersistenceRepositoryAdapter implements PersistenceRepository {

    private static final Logger log = LoggerFactory.getLogger(PersistenceRepositoryAdapter.class);

    @Override
    public void persist(IoTask ioTask) {
        log.debug("Persisted blocking task UUID: {}", ioTask.getId());
    }
}
