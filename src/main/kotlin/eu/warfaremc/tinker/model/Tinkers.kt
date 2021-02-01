package eu.warfaremc.tinker.model

import org.jetbrains.exposed.sql.Table

object Tinkers : Table("t_tinkers") {
    val uid = varchar("uid", 36)
    val data = blob("data").nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(uid, name = "PKTinkersID")
}