package de.menkalian.crater.server.database.task.dao

import de.menkalian.crater.data.task.Gimmick
import de.menkalian.crater.server.database.shared.dao.EnumDataTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

object GimmickData : EnumDataTable() {
    class GimmickDataEntry(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<GimmickDataEntry>(GimmickData) {
            fun Gimmick.findDao(): GimmickDataEntry {
                return find { GimmickData.name.eq(this@findDao.name) }.first()
            }
        }

        var name by GimmickData.name

        fun toEnum(): Gimmick {
            return Gimmick.valueOf(name)
        }
    }
}