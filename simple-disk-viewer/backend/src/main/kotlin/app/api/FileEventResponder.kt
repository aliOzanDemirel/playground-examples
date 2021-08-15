package app.api

import app.JsonMapper
import app.dto.toFileResponse
import com.fasterxml.jackson.core.PrettyPrinter
import dev.vishna.watchservice.KWatchEvent
import io.ktor.application.ApplicationCall
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.response.cacheControl
import io.ktor.response.respondTextWriter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("FileEventResponder")
private val singleLineJsonWriter = JsonMapper.mapper.writer(null as PrettyPrinter?)

@ExperimentalCoroutinesApi
suspend fun ApplicationCall.respondKWatchEvent(kWatchEvents: ReceiveChannel<KWatchEvent>) {

    response.cacheControl(CacheControl.NoCache(null))

    respondTextWriter(ContentType.Text.EventStream) {

        kWatchEvents.consumeEach { event ->

            log.debug("Consumed file event ${event.kind} for ${event.file.path}")

            if (event.kind !== KWatchEvent.Kind.Initialized) {

                if (event.kind === KWatchEvent.Kind.Deleted) {

                    write("data: ${event.file.path}\n")
                } else {

                    val fileResponse = event.file.toFileResponse()
                    write("data: ${singleLineJsonWriter.writeValueAsString(fileResponse)}\n")
                }
            }
            write("event: ${event.kind}\n")
            write("\n")
            flush()
        }
    }
}
