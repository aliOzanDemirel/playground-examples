package app.config

import com.google.inject.Inject
import groovy.util.logging.Slf4j
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import ratpack.service.Service
import ratpack.service.StartEvent

@Slf4j
class StartupConfiguration implements Service {

    private final LogConfig logConfig

    @Inject
    StartupConfiguration(LogConfig logConfig) {
        this.logConfig = logConfig
    }

    @Override
    void onStart(StartEvent event) {

        log.info("Applying startup configuration...")

        if (logConfig.debugEnabled) {
            log.info("Enabling debug log level for application code...")

            Configurator.setLevel("app", Level.DEBUG)
            Configurator.setLevel("business", Level.DEBUG)
        }
    }
}
