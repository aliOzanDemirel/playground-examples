package app.service

import app.exception.AccessDenied
import app.exception.NotFound
import dev.vishna.watchservice.KWatchChannel
import dev.vishna.watchservice.asWatchChannel
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.AccessDeniedException
import java.nio.file.NoSuchFileException

object DirectoryObserver {

    private val log = LoggerFactory.getLogger(javaClass)

    internal fun getChannel(path: String): KWatchChannel {

        try {
            val dir = File(path)
            if (!dir.isDirectory) {
                throw Exception("$path is not a directory, it won't be observed")
            }

            log.info("Starting to observe directory: ${dir.absolutePath}")
            return dir.asWatchChannel(mode = KWatchChannel.Mode.SingleDirectory)

        } catch (e: IOException) {

            when (e) {
                is AccessDeniedException, is kotlin.io.AccessDeniedException -> {
                    throw AccessDenied("Cannot observe, application access is denied for '$path'", e)
                }
                is NoSuchFileException -> {
                    throw NotFound("'$path' is not found, it may be invalid!", e)
                }
                else -> throw Exception("Exception occurred when processing file path ${path}!", e)
            }
        }
    }
}
