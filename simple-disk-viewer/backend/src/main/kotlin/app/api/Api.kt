package app.api

import app.dto.ListWrapper
import app.service.DirectoryObserver
import app.service.FileService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext

internal fun Routing.roots() {
    get("/roots") {
        withContext(Dispatchers.IO) {
            call.respond(ListWrapper(FileService.getRootPaths()))
        }
    }
}

internal fun Routing.files() {

    route("/files") {

        get {

            withContext(Dispatchers.IO) {

                val depth = call.paramShouldBePositive("depth")
                val path = call.paramShouldExist("path")
                val pageSize = call.paramIntIfExists("pageSize")
                val pageIndex = call.paramIntIfExists("pageIndex")

                // TODO: can be separated into 2 different endpoints (paged children and deep scan)
                //  as paging with deep scan does not seem useful at all
                val response = FileService.getFilesAndFolders(path, depth, pageSize, pageIndex)
                call.respond(response)
            }
        }
        delete {

            withContext(Dispatchers.IO) {

                val path = call.paramShouldExist("path");
                FileService.deleteFileOrFolder(path)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

@ExperimentalCoroutinesApi
internal fun Routing.observe() {

    get("/observe") {

        withContext(Dispatchers.IO) {

            val path = call.paramShouldExist("path");
            val watchChannel = DirectoryObserver.getChannel(path)

            try {
                call.respondKWatchEvent(watchChannel)
            } finally {
                watchChannel.cancel()
            }
        }
    }
}
