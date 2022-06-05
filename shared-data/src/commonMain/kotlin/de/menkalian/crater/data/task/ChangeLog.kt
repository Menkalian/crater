package de.menkalian.crater.data.task

@kotlinx.serialization.Serializable
data class ChangeLog(
    val oldVersion: Long,
    val newVersion: Long,

    val addedTasks: List<Task>,
    val removedIds: List<Long>
)