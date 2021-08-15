package app.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class FileResponse(
    val path: String,
    val sizeInBytes: Long,

    val created: Long,
    val modified: Long,

    @get:JsonProperty("isHidden") val isHidden: Boolean = false,
    @get:JsonProperty("isFolder") val isFolder: Boolean = false,

    val children: MutableList<FileResponse>
)

fun File.toFileResponse(children: MutableList<File> = mutableListOf()): FileResponse {

    val fileAttr = Files.readAttributes(this.toPath(), BasicFileAttributes::class.java)
    return FileResponse(
        this.absolutePath,
        fileAttr.size(),
        fileAttr.creationTime().toMillis(),
        fileAttr.lastModifiedTime().toMillis(),
        this.isHidden,
        fileAttr.isDirectory,
        children.map { it.toFileResponse() }.toMutableList()
    )
}
