import app.api.CpuRequestHandler
import app.api.IoRequestHandler
import app.config.LogConfig
import app.config.ServiceModule
import app.config.StartupConfiguration
import org.slf4j.LoggerFactory
import ratpack.handling.RequestLogger

import static ratpack.groovy.Groovy.ratpack

ratpack {

    serverConfig {
        port(8080)

        // 2 MiB limit for request body
        maxContentLength(2_097_152)

        // sysProps will override props as defined order here prevails
        props("app.properties")
        sysProps('app.')

        require("/log", LogConfig)
    }

    bindings {
        module ServiceModule
        bind StartupConfiguration
    }

    handlers { IoRequestHandler ioRequestHandler, CpuRequestHandler cpuRequestHandler, LogConfig logConfig ->

        if (logConfig.httpRequest) {
            println("Enabling request logging for all handlers")
            all {
                context.insert(RequestLogger.ncsa(LoggerFactory.getLogger('RequestLogger')))
            }
        }

        get("io", ioRequestHandler)
        post("cpu", cpuRequestHandler)
    }
}