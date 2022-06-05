package de.menkalian.crater.server.database.task.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object VersionData : LongIdTable() {
    val begin = VersionData.long("begin")
    val end = VersionData.long("end")

    class VersionDataEntry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<VersionDataEntry>(VersionData)

        var begin by VersionData.begin
        var end by VersionData.end
    }
}