package de.menkalian.crater.server.rest.tasks

import de.menkalian.crater.data.task.ChangeLog
import de.menkalian.crater.data.task.Task
import de.menkalian.crater.server.database.task.ITaskDatabase
import de.menkalian.crater.server.util.InvalidDataException
import de.menkalian.crater.server.util.NotFoundException
import de.menkalian.crater.server.util.logger
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class TaskCrudRestController(private val database: ITaskDatabase) {
    @PutMapping("task")
    fun createNewTask(@RequestBody task: Task): Task {
        if (task.difficulty !in 1..10) {
            throw InvalidDataException("Difficulty must be in Range [1;10]")
        }
        logger().debug("Creating new task $task")
        return database.createTask(task)
    }

    @GetMapping("task/all")
    fun getAllActiveTasks(): List<Task> {
        return database.getAllTasks()
    }

    @GetMapping("task/{id}")
    fun getTask(@PathVariable("id") id: Long): Task {
        return database.getTask(id) ?: throw NotFoundException()
    }

    @DeleteMapping("task/{id}")
    fun removeTask(@PathVariable("id") id: Long): Boolean {
        return database.removeTask(id)
    }

    @GetMapping("version/content")
    fun getCurrentContentVersion(): Long {
        return database.getCurrentVersion()
    }

    @GetMapping("version/patch")
    fun getChangeLog(
        @RequestHeader("startVersion", defaultValue = "1") startVersion: String,
        @RequestHeader("targetVersion", defaultValue = "") targetVersion: String
    ): List<ChangeLog> {
        val startVersionL = startVersion.toLongOrNull() ?: 1L
        val currentVersion = database.getCurrentVersion()
        val targetVersionL = targetVersion.toLongOrNull() ?: currentVersion

        if (startVersionL !in 1..currentVersion || targetVersionL !in 1..currentVersion) {
            throw InvalidDataException("Desired version range invalid")
        }

        if (startVersionL > targetVersionL) {
            throw InvalidDataException("Negative version range")
        }

        if (startVersionL == targetVersionL)
            return listOf()

        return database.getChangeLogs(startVersionL, targetVersionL)
    }
}