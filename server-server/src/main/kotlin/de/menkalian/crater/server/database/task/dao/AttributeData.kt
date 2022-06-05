package de.menkalian.crater.server.database.task.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object AttributeData : LongIdTable() {
    val key = AttributeData.varchar("key", 255)
    val value = AttributeData.text("value")
    val task = reference("task", TaskData.id)

    class AttributeDataEntry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<AttributeDataEntry>(AttributeData)

        var key by AttributeData.key
        var value by AttributeData.value
        var task by TaskData.TaskDataEntry referencedOn AttributeData.task
    }
}