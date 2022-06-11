package de.menkalian.crater.data.task

import kotlinx.serialization.Serializable

@Serializable
data class ChangeLog(
    val oldVersion: Long,
    val newVersion: Long,

    val addedTasks: List<Task>,
    val removedIds: List<Long>
)