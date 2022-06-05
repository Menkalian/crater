package de.menkalian.crater.server.rest.guesstimate

import de.menkalian.crater.data.task.ChangeLog
import de.menkalian.crater.data.task.Task
import de.menkalian.crater.server.database.task.ITaskDatabase
import de.menkalian.crater.server.util.NotFoundException
import de.menkalian.crater.server.util.logger
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class TaskCrudRestController(private val database: ITaskDatabase) {
    @PutMapping("task")
    fun createNewTask(task: Task): Task {
        logger().debug("Creating new task $task")
        return database.createTask(task)
    }

    @GetMapping("task/all")
    fun getAllActiveTasks(): List<Task> {
        return database.getAllTasks()
    }

    @GetMapping("task/{id}")
    fun getAllActiveTasks(@PathVariable("id") id: Long): Task {
        return database.getTask(id) ?: throw NotFoundException()
    }

    @DeleteMapping("task/{id}")
    fun removeTask(@PathVariable("id") id: Long): Boolean {
        return database.removeTask(id)
    }

    @GetMapping("task/content/version")
    fun getCurrentContentVersion(): Long {
        return database.getCurrentVersion()
    }

    @GetMapping("task/patch")
    fun getChangeLog(
        @RequestHeader("startVersion", defaultValue = "1") startVersion: String,
        @RequestHeader("targetVersion", defaultValue = "") targetVersion: String
    ): List<ChangeLog> {
        val startVersionL = startVersion.toLongOrNull() ?: 1L
        val targetVersionL = targetVersion.toLongOrNull() ?: database.getCurrentVersion()
        return database.getChangeLogs(startVersionL, targetVersionL)
    }
}