package app;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class Main {

    public static void main(final String... args) {

        var options = new VertxOptions();
        options.setWorkerPoolSize(200);

        // 500 milliseconds
        options.setBlockedThreadCheckInterval(500);
        Vertx vertx = Vertx.vertx(options);

        var deploymentOptions = new DeploymentOptions().setWorker(isBlockingIoEnabled());
        vertx.deployVerticle(new AppVerticle(), deploymentOptions);
    }

    public static boolean isBlockingIoEnabled() {

        return Boolean.parseBoolean(System.getProperty("enableBlockingIo"));
    }
}