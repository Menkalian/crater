package de.menkalian.crater.data.task

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Long,

    val createdAt: Long,
    val removedAt: Long,

    val language: Language,
    val difficulty: Int,
    val category: Category,

    val text: String,
    val severityMultiplier: Double,

    val gimmickTexts: Map<Gimmick, String> = mapOf(),
    val attributes: Map<String, String>
)
