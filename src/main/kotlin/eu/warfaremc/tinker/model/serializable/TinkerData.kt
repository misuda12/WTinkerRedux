package eu.warfaremc.tinker.model.serializable

import eu.warfaremc.tinker.model.serializers.AnyValueSerializer
import eu.warfaremc.tinker.model.serializers.MapStringAnySerializer
import eu.warfaremc.tinker.model.serializers.UUIDSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import java.io.Serializable
import java.util.*

@kotlinx.serialization.Serializable
data class TinkerData(
    @kotlinx.serialization.Serializable(with = UUIDSerializer::class) val uid: UUID,
    val entries: MutableMap<String, @kotlinx.serialization.Serializable(with = AnyValueSerializer::class) Any?> = Collections.emptyMap()
) : Serializable {
    companion object {
        @ExperimentalSerializationApi
        private val cbor = Cbor(Cbor) { }

        @ExperimentalSerializationApi
        @JvmStatic
        fun serialize(data: TinkerData?): ByteArray? {
            if(data == null)
                return null
            return cbor.encodeToByteArray(MapStringAnySerializer, data.entries);
        }

        @ExperimentalSerializationApi
        @JvmStatic
        fun deserialize(uid: UUID?, array: ByteArray?): TinkerData? {
            if(uid == null)
                return null
            if(array == null)
                return TinkerData(uid, Collections.emptyMap())

            return TinkerData(uid, cbor.decodeFromByteArray(MapStringAnySerializer, array).toMutableMap());
        }
    }

    operator fun get(key: String?): Any? = entries[key]

    operator fun set(key: String?, value: Any?) {
        if (key == null)
            return
        entries[key] = value
    }
}