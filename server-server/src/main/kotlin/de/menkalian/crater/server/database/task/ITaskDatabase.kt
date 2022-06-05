package de.menkalian.crater.server.database.task

import de.menkalian.crater.data.task.ChangeLog
import de.menkalian.crater.data.task.Task
import de.menkalian.crater.server.database.IDatabase

interface ITaskDatabase : IDatabase {
    // CREATE
    fun createTask(task: Task): Task

    // READ
    fun getAllTasks(): List<Task>
    fun getTask(id: Long): Task?

    fun getChangeLogs(oldVersion: Long, newVersion: Long): List<ChangeLog>

    // DELETE
    fun removeTask(id: Long): Boolean

    // MANAGEMENT
    fun getCurrentVersion(): Long
    fun updateVersion()
}