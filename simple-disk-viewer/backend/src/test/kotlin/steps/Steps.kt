package steps

import app.dto.FileResponse
import app.dto.ListWrapper
import app.main
import app.service.FileService
import app.shutdown
import io.cucumber.core.api.Scenario
import io.cucumber.java8.En
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.http.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

// tests should not be run concurrently
private var serverStarted: Boolean = false

@ExperimentalCoroutinesApi
class Steps : En {

    private val log = LoggerFactory.getLogger(javaClass)

    private var rootTempDir: Path? = null
    private var userDir: String = System.getProperty("user.home")
    private var isWindows: Boolean = true

    // tests should not be run concurrently
    private var responseStatus: HttpStatusCode? = null
    private var responseBody: Any? = null
    private val client: HttpClient = HttpClient(Apache) {
        install(JsonFeature)
    }

    init {
        Before { _: Scenario ->
            if (!serverStarted) {
                main()
                serverStarted = true

                val osName = System.getProperty("os.name")
                isWindows = osName.startsWith("Windows")
                log.info("server is started: $serverStarted, isWindows: $isWindows")

                Runtime.getRuntime().addShutdownHook(object : Thread() {
                    override fun run() {
                        shutdown()
                    }
                })
            }
        }

        After { _: Scenario ->
            if (rootTempDir != null && Files.exists(rootTempDir)) {
                FileService.deleteFileOrFolder(rootTempDir!!)
            }
        }

        When("^file details of \"([^\"]*)\" is requested with depth (\\d+)$")
        { filePath: String, depthLevel: Int ->
            runBlocking {
                client.use {
                    val actualFilePath = Paths.get(userDir, filePath)
                    val response = it.get<HttpResponse>(
                        "http://localhost:8080/files?path=$actualFilePath&depth=$depthLevel"
                    )
                    responseStatus = response.status
                    responseBody = receiveBody<FileResponse>(response)
                }
            }
        }

        When("^file system roots is requested$") {

            runBlocking {
                client.use {
                    val response = it.get<HttpResponse>(port = 8080, host = "localhost", path = "/roots")
                    responseStatus = response.status
                    responseBody = receiveBody<ListWrapper<String>>(response)
                }
            }
        }

        When("^file \"([^\"]*)\" is requested to be deleted$") { filePath: String ->

            runBlocking {
                client.use {
                    val actualFilePath = Paths.get(userDir, filePath)
                    val response = it.delete<HttpResponse>("http://localhost:8080/files?path=$actualFilePath")
                    responseStatus = response.status
                }
            }
        }

        Given("^temporary directory \"([^\"]*)\"$") { tempPath: String ->

            log.info("creating root temp folder under user directory: $userDir")
            rootTempDir = Files.createDirectories(Paths.get(userDir, tempPath))
            log.info("created root temp folder: ${rootTempDir?.toAbsolutePath()} for test")
        }

        Given("^temporary directory has folder \"([^\"]*)\"$") { tempFolderPath: String ->

            if (rootTempDir != null) {
                val tempFolderToCreate = Paths.get(rootTempDir?.toAbsolutePath().toString(), tempFolderPath)
                val tempFolder = Files.createDirectories(tempFolderToCreate)
                log.info("created temp folder: ${tempFolder?.toAbsolutePath()} for test")
            }
        }

        Given("^temporary directory has file \"([^\"]*)\" under \"([^\"]*)\"$")
        { fileName: String, tempFolderPath: String ->

            if (rootTempDir != null) {
                val tempFolderToPut = Paths.get(rootTempDir?.toAbsolutePath().toString(), tempFolderPath, fileName)
                if (!Files.exists(tempFolderToPut)) {
                    val tempFile = Files.createFile(tempFolderToPut)
                    log.info("created temp file: ${tempFile.toAbsolutePath()} for test")
                }
            }
        }

        Then("^response status is (\\d+)$") { status: Int ->

            assertEquals(responseStatus, HttpStatusCode.fromValue(status))
        }

        Then("^response list should have \"([^\"]*)\"$") { expectedElement: String ->

            val result = responseBody as ListWrapper<String>
            assertNotNull(result)
            assertNotNull(result.content)
            assertFalse(result.content.isEmpty())

            if (isWindows) {
                assertTrue(result.content.any { it == expectedElement })
            } else {
                assertEquals(1, result.content.size)
                assertTrue(result.content.all { it == "/" })
            }
        }

        Then("^file response has parent \"([^\"]*)\" that has child \"([^\"]*)\"$")
        { expectedParentRelative: String, expectedChildRelative: String ->

            val result = assertBodyAsFileResponse()
            var parent: FileResponse? = result
            val expectedParent: String = Paths.get(userDir, expectedParentRelative).toAbsolutePath().toString()
            if (result.path != expectedParent) {

                parent = findInChildren(result, expectedParent)
            }
            assertNotNull("Parent $expectedParent is not found!", parent)

            val expectedChild: String = Paths.get(userDir, expectedChildRelative).toAbsolutePath().toString()
            if (expectedChildRelative.isNotBlank() && expectedChild.isNotBlank()) {
                val child: FileResponse? = findInChildren(parent!!, expectedChild)
                assertNotNull("Child $expectedChild is not found!", child)
            }
        }

        Then("^file response does not have \"([^\"]*)\"$") { filePath: String ->

            val result = assertBodyAsFileResponse()
            val shouldNotExist: FileResponse? =
                if (result.path != filePath) {
                    findInChildren(result, filePath)
                } else {
                    result
                }

            assertNull("$filePath should not have been found in response!", shouldNotExist)
        }

        Then("^file \"([^\"]*)\" does not exist$") { filePath: String ->

            val fileExists = Files.exists(Path.of(filePath))
            assertFalse("$filePath exists in file system!", fileExists)
        }
    }

    private fun findInChildren(fileResponse: FileResponse, pathToCheck: String): FileResponse? {

        if (!fileResponse.children.isNullOrEmpty()) {

            for (child in fileResponse.children) {

                if (child.path == pathToCheck) {
                    return fileResponse
                }
            }

            for (child in fileResponse.children) {

                val resp = findInChildren(child, pathToCheck)

                if (resp != null) {
                    return resp
                }
            }
        }

        return null
    }

    private fun assertBodyAsFileResponse(): FileResponse {

        val result = responseBody as FileResponse
        assertNotNull(result)
        assertNotNull(result.sizeInBytes)
        assertNotNull(result.created)
        assertNotNull(result.modified)
        assertNotNull(result.isFolder)
        assertNotNull(result.isHidden)
        return result
    }

    private inline fun <reified T> receiveBody(response: HttpResponse): T {
        return runBlocking {
            response.receive<T>()
        }
    }
}
