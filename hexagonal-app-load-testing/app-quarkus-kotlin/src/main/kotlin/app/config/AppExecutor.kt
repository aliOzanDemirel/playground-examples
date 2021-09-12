package app.config

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object AppExecutor {
    val io: ExecutorService = Executors.newFixedThreadPool(16);
}
