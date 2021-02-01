package eu.warfaremc.tinker.model.serializable

import eu.warfaremc.tinker.model.serializers.AnyValueSerializer
import eu.warfaremc.tinker.model.serializers.UUIDSerializer
import java.io.Serializable
import java.util.*

@kotlinx.serialization.Serializable
data class TinkerData(
    @kotlinx.serialization.Serializable(with = UUIDSerializer::class) val uid: UUID,
    val data: MutableMap<String, @kotlinx.serialization.Serializable(with = AnyValueSerializer::class) Any?> = Collections.emptyMap()
) : Serializable {
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

    operator fun get(key: String?): Any? = data[key]

    operator fun set(key: String?, value: Any?) {
        if (key == null)
            return
        data[key] = value
    }
}