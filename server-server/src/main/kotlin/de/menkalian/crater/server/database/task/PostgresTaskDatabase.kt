package de.menkalian.crater.server.database.task

import de.menkalian.crater.data.task.Category
import de.menkalian.crater.data.task.ChangeLog
import de.menkalian.crater.data.task.Language
import de.menkalian.crater.data.task.Task
import de.menkalian.crater.server.database.DatabaseHelper
import de.menkalian.crater.server.database.shared.MetaDataAwareDatabaseExtension
import de.menkalian.crater.server.database.task.dao.AttributeData
import de.menkalian.crater.server.database.task.dao.CategoryData
import de.menkalian.crater.server.database.task.dao.CategoryData.CategoryDataEntry.Companion.findDao
import de.menkalian.crater.server.database.task.dao.LanguageData
import de.menkalian.crater.server.database.task.dao.LanguageData.LanguageDataEntry.Companion.findDao
import de.menkalian.crater.server.database.task.dao.TaskData
import de.menkalian.crater.server.database.task.dao.VersionData
import de.menkalian.crater.server.database.task.dao.VersionPatchData
import de.menkalian.crater.server.database.task.dao.VersionPatchItemData
import de.menkalian.crater.server.util.currentUnixSecond
import de.menkalian.crater.server.util.initEnumDatabase
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component
import java.io.File


@Component
@Suppress("LeakingThis")
class PostgresTaskDatabase(
    @Value("\${crater.postgres.database.host}") private val databaseHost: String,
    @Value("\${crater.postgres.database.port}") private val databasePort: String,
    @Value("\${crater.postgres.database.user.name}") private val databaseUsername: String,
    @Value("\${crater.postgres.database.user.password}") private val databasePassword: String,
    @Value("\${crater.postgres.database.tasks.name}") private val databaseName: String,
    @Value("\${crater.postgres.patch.archive.size}") private val patchArchiveSize: Long,
    build: BuildProperties
) : ITaskDatabase {
    companion object {
        private const val DATABASE_SCHEMA_VERSION = 1
        private val VERSION_MUTEX = Any()
    }

    override var isOpen: Boolean = false
    override val dbConnection: Database

    private val metadataExtension = MetaDataAwareDatabaseExtension()

    init {
        File(databaseName).mkdirs()

        DatabaseHelper.createDatabase(databaseName, databaseHost, databasePort, databaseUsername, databasePassword)
        dbConnection = Database.connect(
            "jdbc:postgresql://$databaseHost:$databasePort/$databaseName",
            driver = "org.postgresql.Driver",
            user = databaseUsername,
            password = databasePassword
        )
        isOpen = true

        metadataExtension.initMetadata(this, build, DATABASE_SCHEMA_VERSION, "Tasks")
        initEnums()

        updateVersion()
    }

    private fun initEnums() {
        ensureOpen()
        initEnumDatabase(dbConnection, CategoryData, Category.values().map { it.name })
        initEnumDatabase(dbConnection, LanguageData, Language.values().map { it.name })
    }

    override fun createTask(task: Task): Task {
        ensureOpen()
        return transaction(dbConnection) {
            createAllTables()

            val daoTask = TaskData.TaskDataEntry
                .new {
                    this.createdAt = currentUnixSecond()
                    this.removedAt = Long.MAX_VALUE

                    this.language = task.language.findDao().id
                    this.difficulty = task.difficulty
                    this.category = task.category.findDao().id

                    this.text = task.text
                    this.severityMultiplier = task.severityMultiplier
                }

            task.attributes.forEach {
                AttributeData.AttributeDataEntry
                    .new {
                        this.task = daoTask
                        this.key = it.key
                        this.value = it.value
                    }
            }

            daoTask.toTaskObject()
        }
    }

    override fun getAllTasks(): List<Task> {
        ensureOpen()
        return transaction(dbConnection) {
            createAllTables()

            TaskData.TaskDataEntry
                .find { TaskData.removedAt.greater(currentUnixSecond()) }
                .map { it.toTaskObject() }
                .toList()
        }
    }

    override fun getTask(id: Long): Task? {
        ensureOpen()
        return transaction(dbConnection) {
            createAllTables()

            TaskData.TaskDataEntry
                .findById(id)
                ?.toTaskObject()
        }
    }

    override fun getChangeLogs(oldVersion: Long, newVersion: Long): List<ChangeLog> {
        ensureOpen()
        return transaction(dbConnection) {
            createAllTables()

            val changeLogs = VersionPatchData.VersionPatchDataEntry
                .find { VersionPatchData.from.greaterEq(oldVersion) and VersionPatchData.to.lessEq(newVersion) }
                .map { it.toChangeLogObject() }
                .toMutableList()

            val minCachedId = changeLogs
                .minOfOrNull { it.oldVersion } ?: 1
            if (minCachedId != oldVersion) {
                changeLogs.add(0, buildPatch(oldVersion, minCachedId))
            }
            val maxCachedId = changeLogs
                .maxOfOrNull { it.newVersion } ?: 1
            if (maxCachedId != newVersion) {
                changeLogs.add(buildPatch(maxCachedId, newVersion))
            }

            changeLogs
        }
    }

    override fun removeTask(id: Long): Boolean {
        ensureOpen()
        return transaction(dbConnection) {
            createAllTables()

            val question = TaskData.TaskDataEntry
                .findById(id)

            if (question != null) {
                question.removedAt = currentUnixSecond()
                true
            } else {
                false
            }
        }
    }

    override fun getCurrentVersion(): Long {
        ensureOpen()
        return transaction(dbConnection) {
            createAllTables()
            VersionData
                .select(VersionData.id.eq(VersionData.selectAll().maxOfOrNull { it[VersionData.id] }?.value ?: 1L))
                .firstOrNull()
                ?.get(VersionData.id)?.value ?: 1L
        }
    }

    override fun updateVersion() {
        synchronized(VERSION_MUTEX) {
            ensureOpen()
            transaction(dbConnection) {
                createAllTables()
                val timestamp = currentUnixSecond()
                val oldVersion = getCurrentVersion()

                val oldVersionObject =
                    VersionData.VersionDataEntry
                        .findById(oldVersion)

                if (oldVersionObject == null) {
                    VersionData.VersionDataEntry
                        .new {
                            this.begin = timestamp
                            this.end = Long.MAX_VALUE
                        }
                    return@transaction
                }

                val dbg = TaskData
                    .select {
                        TaskData.createdAt.between(oldVersionObject.begin, timestamp) or
                                TaskData.removedAt.between(oldVersionObject.begin, timestamp)
                    }.toList()
                // If no data was changed we do not need a new version
                if (TaskData
                        .select {
                            TaskData.createdAt.between(oldVersionObject.begin, timestamp) or
                                    TaskData.removedAt.between(oldVersionObject.begin, timestamp)
                        }.none()
                ) {
                    return@transaction
                }

                oldVersionObject.end = timestamp

                val newVersionObject = VersionData.VersionDataEntry
                    .new {
                        this.begin = timestamp
                        this.end = Long.MAX_VALUE
                    }

                if (newVersionObject.id.value % patchArchiveSize == 0L) {
                    val patchesBuiltUntil = VersionData.VersionDataEntry
                        .find { VersionData.id.eq(VersionPatchData.to.max()) }
                        .first()
                    val patchObj = buildPatch(patchesBuiltUntil.id.value, newVersionObject.id.value)

                    val patch = VersionPatchData.VersionPatchDataEntry
                        .new {
                            this.from = patchesBuiltUntil
                            this.to = newVersionObject
                        }

                    patchObj.addedTasks.forEach {
                        VersionPatchItemData.VersionPatchItemDataEntry
                            .new {
                                this.add = true
                                this.task = TaskData.TaskDataEntry[it.id]
                                this.patch = patch
                            }
                    }
                    patchObj.removedIds.forEach {
                        VersionPatchItemData.VersionPatchItemDataEntry
                            .new {
                                this.add = true
                                this.task = TaskData.TaskDataEntry[it]
                                this.patch = patch
                            }
                    }
                }
            }
        }
    }

    private fun buildPatch(startVersion: Long, targetVersion: Long): ChangeLog {
        val startVersionObj = VersionData.VersionDataEntry
            .find { VersionData.id.eq(startVersion) }
            .first()
        val targetVersionObj = VersionData.VersionDataEntry
            .find { VersionData.id.eq(targetVersion) }
            .first()

        val addedTasks = TaskData.TaskDataEntry
            .find { TaskData.createdAt.between(startVersionObj.begin, targetVersionObj.begin - 1) }
            .toMutableList()
        val removedTasks = TaskData.TaskDataEntry
            .find { TaskData.removedAt.between(startVersionObj.begin, targetVersionObj.begin - 1) }
            .toMutableList()

        val irrelevantTasks = addedTasks.intersect(removedTasks)
        addedTasks.removeAll(irrelevantTasks)
        removedTasks.removeAll(irrelevantTasks)

        return ChangeLog(
            startVersionObj.id.value,
            targetVersionObj.id.value,
            addedTasks.map { it.toTaskObject() },
            removedTasks.map { it.id.value }
        )
    }

    private fun createAllTables() {
        SchemaUtils.create(CategoryData, LanguageData, TaskData, AttributeData, VersionData, VersionPatchData, VersionPatchItemData)
    }

    override fun close() {
        isOpen = false
        TransactionManager.closeAndUnregister(dbConnection)
    }
}
