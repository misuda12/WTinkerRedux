@file:UseSerializers(UUIDSerializer::class)
package eu.warfaremc.tinker.model

import eu.warfaremc.tinker.model.serializable.UUIDSerializer
import kotlinx.serialization.UseSerializers
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import java.io.Serializable
import java.util.*

object Tinkers : Table("t_tinkers") {
    val uid = varchar("uid", 36);
    val data = blob("data").nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(uid, name = "PKTinkersID")
}

@kotlinx.serialization.Serializable
data class TinkerData(
    val uid: UUID,
    val data: MutableMap<String, Any> = Collections.emptyMap()
) : Serializable
{
    companion object {
        @JvmStatic
        fun serialize(data: TinkerData?): ByteArray? {
            TODO("Not implemented")
        }

        @JvmStatic
        fun deserialize(uid: UUID, array: ByteArray?): TinkerData? {
            TODO("Not implemented")
        }
    }
    operator fun get(key: String?): Any?
        = data[key]

    operator fun set(key: String, value: Any)
       = value.also { data[key] = it };
}