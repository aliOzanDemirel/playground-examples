package app.service

import app.dto.FileResponse
import app.dto.toFileResponse
import app.exception.AccessDenied
import app.exception.NotFound
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

object FileService {

    private val log = LoggerFactory.getLogger(javaClass)

    internal fun deleteFileOrFolder(filePath: String) {

        deleteFileOrFolder(Paths.get(filePath))
    }

    internal fun deleteFileOrFolder(filePath: Path) {

        if (!Files.exists(filePath)) {
            throw Exception("No file exists for given path: $filePath")
        }

        // removes files when traversing and when leaving directory removes empty directory
        Files.walkFileTree(filePath, object : SimpleFileVisitor<Path>() {

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.delete(file)
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                Files.delete(dir)
                return FileVisitResult.CONTINUE
            }
        })

        log.info("{} is deleted", filePath.toAbsolutePath())
    }

    /**
     * @return absolute paths of filesystem roots of the underlying host machine.
     */
    internal fun getRootPaths(): List<String> {

        return File.listRoots().map { it.absolutePath }.toList()
    }

    /**
     * @param pageSize if not null, pagination will be applied to the children of root.
     *                 page includes elements as depth-first instead of direct children
     *
     * @return file response for single file or file response as tree if the path corresponds to folder.
     */
    internal fun getFilesAndFolders(
        path: String, depth: Int = 1,
        pageSize: Int?, pageIndex: Int?
    ): FileResponse {

        try {

            val file = File(path)
            val fileResponse = if (!file.isDirectory) {
                file.toFileResponse()
            } else {
                walkFiles(file, depth, pageSize, pageIndex)
            }

            log.debug("File response for depth: $depth and path: $path is:\n$fileResponse")
            return fileResponse

        } catch (e: Exception) {

            when (e) {
                is AccessDeniedException, is kotlin.io.AccessDeniedException -> {
                    throw AccessDenied("Application access is denied for '$path'", e)
                }
                is NoSuchFileException -> {
                    throw NotFound("'$path' is not found, it may be invalid!", e)
                }
                else -> throw Exception("Exception occurred when processing file path ${path}!", e)
            }
        }
    }

    private fun walkFiles(
        rootFile: File, depth: Int,
        pageSize: Int?, pageIndex: Int?
    ): FileResponse {

        log.debug("Depth $depth will be used to traverse files")
        val lazyFileWalker = rootFile.walk()
            .maxDepth(depth)
            .onEnter { file ->
                try {
                    return@onEnter file.canRead()
                } catch (e: Exception) {
                    log.warn("Could not enter ${file.absolutePath}, skipping it", e)
                    return@onEnter false
                }

            }.onFail { file, exception ->
                // TODO: can flag this file to show that there may be children but they cannot be accessed
                log.error("Error occurred when walking ${file.absolutePath}", exception)
            }

        val parentToChildrenMap = scanAndGetMapResult(lazyFileWalker, rootFile, pageSize, pageIndex)
        return buildDeepFileResponse(rootFile, parentToChildrenMap)
    }

    private fun scanAndGetMapResult(
        fileWalker: FileTreeWalk, rootFile: File,
        pageSize: Int?, pageIndex: Int?
    ): MutableMap<File, MutableList<File>> {

        val parentToChildrenMap = mutableMapOf<File, MutableList<File>>(rootFile to mutableListOf())

        if (pageSize != null) {

            // if pageSize is provided, results will be paginated
            // if no index is provided, default result is the first page

            // plus 1 is for the root file that the walker starts from
            val skippedElementCount = (pageSize * (pageIndex ?: 0)) + 1
            var count = 0
            fileWalker
                .drop(skippedElementCount)
                .takeWhile { pageSize > count++ }
                .forEach { putEnteredFileToMap(it, parentToChildrenMap) }
        } else {

            fileWalker
                .filter { it.path != rootFile.path }
                .forEach { putEnteredFileToMap(it, parentToChildrenMap) }
        }

        return parentToChildrenMap
    }

    private fun putEnteredFileToMap(file: File, parentToChildrenMap: MutableMap<File, MutableList<File>>) {

        val parentFile = file.parentFile;
        val childrenOfParent = parentToChildrenMap.getOrPut(parentFile, { mutableListOf() })
        childrenOfParent.add(file)
    }

    private fun buildDeepFileResponse(
        rootFile: File,
        parentToChildrenMap: MutableMap<File, MutableList<File>>
    ): FileResponse {

        val rootFileResponse = rootFile.toFileResponse()
        val childrenOfRoot = parentToChildrenMap[rootFile]
        if (childrenOfRoot != null) {
            putToChildrenInTree(parentToChildrenMap, childrenOfRoot, rootFileResponse)
        }
        return rootFileResponse
    }

    private fun putToChildrenInTree(
        fileMap: Map<File, MutableList<File>>,
        childrenOfParent: MutableList<File>,
        parentResponse: FileResponse
    ) {

        for (childOfRoot in childrenOfParent) {

            val childResponse = childOfRoot.toFileResponse()
            parentResponse.children.add(childResponse)

            val childrenOfChild = fileMap[childOfRoot]
            if (childrenOfChild != null) {
                putToChildrenInTree(fileMap, childrenOfChild, childResponse)
            }
        }
    }

}
