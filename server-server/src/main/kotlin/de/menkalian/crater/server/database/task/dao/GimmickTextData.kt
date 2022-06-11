package de.menkalian.crater.server.database.task.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object GimmickTextData : LongIdTable() {
    val key = reference("key", GimmickData.id)
    val value = GimmickTextData.text("value")
    val task = reference("task", TaskData.id)

    class GimmickTextDataEntry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<GimmickTextDataEntry>(GimmickTextData)

        var key by GimmickData.GimmickDataEntry referencedOn GimmickTextData.key
        var value by GimmickTextData.value
        var task by TaskData.TaskDataEntry referencedOn GimmickTextData.task
    }
}