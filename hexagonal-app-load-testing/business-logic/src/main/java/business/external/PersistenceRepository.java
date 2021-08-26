package business.external;

import business.entity.IoTask;
import business.port.ExternalAdapterApi;

@ExternalAdapterApi
public interface PersistenceRepository {

    void persist(IoTask ioTask);
}
