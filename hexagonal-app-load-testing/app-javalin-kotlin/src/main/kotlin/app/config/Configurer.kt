package app.config

import io.javalin.core.JavalinConfig
import io.javalin.core.JettyUtil
import org.eclipse.jetty.util.thread.QueuedThreadPool
import java.util.function.Consumer

val javalinAppConfigurer = Consumer<JavalinConfig> { config ->

    // configure default jetty server's thread pool boundaries
    config.server {
        val server = JettyUtil.getOrDefault(null)
        val pool = server.threadPool
        if (pool is QueuedThreadPool) {
            pool.maxThreads = 100
            pool.minThreads = 10
        }
        server
    }

    // 2 MB
    config.maxRequestSize = 2 * 1024 * 1024
    config.logIfServerNotStarted = true
}