package app;

import app.api.CpuTaskResource;
import app.api.IoTaskResource;
import app.config.Configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

// there is no need to have a subclass of jax-rs application class in helidon, framework generates it with auto scanned resources
@ApplicationScoped
@ApplicationPath("/")
public class HelidonApp extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                // providers
                Configuration.class,
                // paths
                IoTaskResource.class,
                CpuTaskResource.class
        );
    }
}