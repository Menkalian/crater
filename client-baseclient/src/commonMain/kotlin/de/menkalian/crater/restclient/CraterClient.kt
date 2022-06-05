package de.menkalian.crater.restclient

import de.menkalian.crater.data.task.ChangeLog
import de.menkalian.crater.data.task.Task
import de.menkalian.crater.data.telemetrie.TelemetrieReport
import de.menkalian.crater.restclient.config.CraterClientConfiguration
import de.menkalian.crater.restclient.error.CraterError
import de.menkalian.crater.restclient.error.CraterException
import de.menkalian.crater.restclient.error.catchErrors
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpRedirect
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Suppress("unused")
class CraterClient(httpClientTemplate: HttpClient, private val configuration: CraterClientConfiguration) : ICraterClient {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val httpClient: HttpClient

    private val log = configuration.logger

    val telemetrie = TelemetrieClient()
    val task = TaskClient()
    val version = VersionClient()

    init {
        log.info("Initializing CraterClient")
        httpClient = httpClientTemplate.config {
            install(HttpTimeout)
            install(HttpRedirect)
            install(JsonFeature) {
                serializer = KotlinxSerializer(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = configuration.ktorLogger
                level = configuration.logLevel
            }

            defaultRequest {
                log.debug("Configuring default request")
                setCraterUrl()
                setTimeouts()
                contentType(ContentType.Application.Json)
            }
        }
    }

    override fun checkUpToDate(currentDatabaseVersion: Long, callback: ICraterClient.VersionCallback): Job {
        return version.getUpstreamVersion(
            onRead = { v ->
                callback.onVersion(currentDatabaseVersion == v, v)
            },
            onError = { throw CraterException(it) }
        )
    }

    override fun getUpstreamVersion(callback: ICraterClient.VersionCallback): Job {
        return version.getUpstreamVersion(
            onRead = { v ->
                callback.onVersion(true, v)
            },
            onError = { throw CraterException(it) }
        )
    }

    override fun getPatchChangeLogs(currentDatabaseVersion: Long, targetVersion: Long?, callback: ICraterClient.ChangelogCallback): Job {
        return version.getPatches(
            currentDatabaseVersion,
            targetVersion,
            onRead = { changes ->
                callback.onChanges(changes.maxOfOrNull { it.newVersion } ?: 1, changes)
            },
            onError = { throw CraterException(it) }
        )
    }

    override fun saveTaskInUpstreamDatabase(task: Task, callback: ICraterClient.TaskCallback): Job {
        return this.task.create(
            task,
            onCreated = {
                callback.onTask(it)
            },
            onError = { throw CraterException(it) }
        )
    }

    override fun getTask(id: Long, callback: ICraterClient.TaskCallback): Job {
        return this.task.getById(
            id,
            onRead = {
                callback.onTask(it)
            },
            onError = { throw CraterException(it) }
        )
    }

    override fun getAllTasks(callback: ICraterClient.TaskListCallback): Job {
        return this.task.getAll(
            onRead = {
                callback.onTaskList(it)
            },
            onError = { throw CraterException(it) }
        )
    }

    override fun removeTask(id: Long, callback: ICraterClient.BooleanCallback): Job {
        return this.task.delete(
            id,
            onFinished = {
                callback.onCompleted(it)
            },
            onError = { throw CraterException(it) }
        )
    }

    override fun uploadTelemetrieReport(report: TelemetrieReport, callback: ICraterClient.BooleanCallback): Job {
        return telemetrie.startUpload(
            report,
            onFinished = {
                callback.onCompleted(true)
            },
            onError = { throw CraterException(it) }
        )
    }

    private fun HttpRequestBuilder.setCraterUrl() {
        url {
            protocol = if (configuration.useHttps) {
                log.trace("Using HTTPS")
                URLProtocol.HTTPS
            } else {
                log.trace("Using HTTP")
                URLProtocol.HTTP
            }
            host = configuration.serverUrl
            port = configuration.serverPort.toInt()
            log.trace("Using host \"$host\" and port \"$port\"")
        }
    }

    private fun HttpRequestBuilder.setTimeouts() {
        timeout {
            requestTimeoutMillis = configuration.requestTimeoutMs
            connectTimeoutMillis = configuration.connectionTimeoutMs
        }
    }

    inner class TelemetrieClient {
        fun startUpload(
            report: TelemetrieReport,
            onFinished: () -> Unit,
            onError: (CraterError) -> Unit
        ): Job {
            return coroutineScope.launch {
                catchErrors(onError) {
                    httpClient.post<Any?> {
                        url.path("telemetrie/upload")
                        body = report
                    }
                    onFinished()
                }
            }
        }
    }

    inner class TaskClient {
        private val base = "task"

        fun create(
            task: Task,
            onCreated: (Task) -> Unit,
            onError: (CraterError) -> Unit
        ): Job {
            return coroutineScope.launch {
                catchErrors(onError) {
                    val created: Task = httpClient.put {
                        url.path(base)
                        body = task
                    }
                    onCreated(created)
                }
            }
        }

        fun getAll(
            onRead: (List<Task>) -> Unit,
            onError: (CraterError) -> Unit
        ): Job {
            return coroutineScope.launch {
                catchErrors(onError) {
                    val read: List<Task> = httpClient.get {
                        url.path(base, "all")
                    }
                    onRead(read)
                }
            }
        }

        fun getById(
            id: Long,
            onRead: (Task) -> Unit,
            onError: (CraterError) -> Unit
        ): Job {
            return coroutineScope.launch {
                catchErrors(onError) {
                    val read: Task = httpClient.get {
                        url.path(base, id.toString())
                    }
                    onRead(read)
                }
            }
        }

        fun delete(
            id: Long,
            onFinished: (Boolean) -> Unit,
            onError: (CraterError) -> Unit
        ): Job {
            return coroutineScope.launch {
                catchErrors(onError) {
                    val success: Boolean = httpClient.delete {
                        url.path(base, id.toString())
                    }
                    onFinished(success)
                }
            }
        }
    }

    inner class VersionClient {
        private val base = "version"

        fun getUpstreamVersion(
            onRead: (Long) -> Unit,
            onError: (CraterError) -> Unit
        ): Job {
            return coroutineScope.launch {
                catchErrors(onError) {
                    val version: Long = httpClient.get {
                        url.path(base, "content")
                    }
                    onRead(version)
                }
            }
        }

        fun getPatches(
            startVersion: Long,
            targetVersion: Long?,
            onRead: (List<ChangeLog>) -> Unit,
            onError: (CraterError) -> Unit
        ): Job {
            return coroutineScope.launch {
                catchErrors(onError) {
                    val read: List<ChangeLog> = httpClient.get {
                        url.path(base, "patch")
                        header("startVersion", startVersion.toString())
                        if (targetVersion != null) {
                            header("targetVersion", targetVersion.toString())
                        }
                    }
                    onRead(read)
                }
            }
        }
    }
}