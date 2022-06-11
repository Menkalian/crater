package de.menkalian.crater.server.database.task.dao

import de.menkalian.crater.data.task.Task
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object TaskData : LongIdTable() {
    val createdAt = long("createdAt")
    val removedAt = long("removedAt")

    val language = reference("language", LanguageData.id)
    val difficulty = integer("difficulty")
    val category = reference("category", CategoryData.id)

    val text = text("text")
    val severityMultiplier = double("severityMultiplier")

    class TaskDataEntry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<TaskDataEntry>(TaskData)

        var createdAt by TaskData.createdAt
        var removedAt by TaskData.removedAt

        var language by TaskData.language
        var difficulty by TaskData.difficulty
        var category by TaskData.category

        var text by TaskData.text
        var severityMultiplier by TaskData.severityMultiplier

        val gimmickTexts by GimmickTextData.GimmickTextDataEntry referrersOn GimmickTextData.task
        val attributes by AttributeData.AttributeDataEntry referrersOn AttributeData.task

        fun toTaskObject(): Task {
            return Task(
                id.value,
                createdAt,
                removedAt,
                LanguageData.LanguageDataEntry[language].toEnum(),
                difficulty,
                CategoryData.CategoryDataEntry[category].toEnum(),
                text,
                severityMultiplier,

                gimmickTexts.associate { it.key.toEnum() to it.value },
                attributes.associate { it.key to it.value }
            )
        }
    }
}