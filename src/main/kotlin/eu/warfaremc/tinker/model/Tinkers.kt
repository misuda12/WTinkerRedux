package eu.warfaremc.tinker.model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import java.io.Serializable
import java.util.*

object Tinkers : Table("t_tinkers") {
    val uid = varchar("uid", 64);
    val data = blob("data").nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(uid, name = "PKTinkersID")
}

data class TinkerData(
    val uid: UUID,
    val data: ExposedBlob?,
) : Serializable