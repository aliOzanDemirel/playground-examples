package app;

import app.api.CpuHandler;
import app.api.IoHandler;
import app.config.DependencyBinding;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Main extends Application<Configuration> {

    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    public String getName() {
        return "app-dropwizard-java";
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {

        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
    }

    @Override
    public void run(Configuration configuration, Environment environment) {

        environment.jersey().register(new DependencyBinding());

        // resources are instantiated and injected
        environment.jersey().register(IoHandler.class);
        environment.jersey().register(CpuHandler.class);
    }
}