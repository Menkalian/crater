package de.menkalian.crater.server.database.task.dao

import de.menkalian.crater.data.task.ChangeLog
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object VersionPatchData : LongIdTable() {
    val from = VersionPatchData.reference("from", VersionData.id)
    val to = VersionPatchData.reference("to", VersionData.id)

    class VersionPatchDataEntry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<VersionPatchDataEntry>(VersionPatchData)

        var from by VersionData.VersionDataEntry referencedOn VersionPatchData.from
        var to by VersionData.VersionDataEntry referencedOn VersionPatchData.to
        val items by VersionPatchItemData.VersionPatchItemDataEntry referrersOn VersionPatchItemData.patch

        fun toChangeLogObject(): ChangeLog {
            return ChangeLog(
                from.id.value,
                to.id.value,
                items.filter { it.add }.map { it.task.toTaskObject() },
                items.filter { it.add.not() }.map { it.task.id.value }
            )
        }
    }
}