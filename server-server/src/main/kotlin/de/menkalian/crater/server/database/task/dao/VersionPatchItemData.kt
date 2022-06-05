package de.menkalian.crater.server.database.task.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object VersionPatchItemData : LongIdTable() {
    val patch = VersionPatchItemData.reference("patch", VersionPatchData.id)
    val task = VersionPatchItemData.reference("task", TaskData.id)
    val add = VersionPatchItemData.bool("add")

    class VersionPatchItemDataEntry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<VersionPatchItemDataEntry>(VersionPatchItemData)

        var patch by VersionPatchData.VersionPatchDataEntry referencedOn VersionPatchItemData.patch
        var task by TaskData.TaskDataEntry referencedOn VersionPatchItemData.task
        var add by VersionPatchItemData.add
    }
}