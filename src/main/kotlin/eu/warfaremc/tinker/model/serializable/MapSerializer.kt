package eu.warfaremc.tinker.model.serializable

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class MapSerializer(override val descriptor: SerialDescriptor) : KSerializer<Map<String, Any>> {
    override fun deserialize(decoder: Decoder): Map<String, Any> {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: Map<String, Any>) {
        TODO("Not yet implemented")
    }
}