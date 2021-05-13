package eu.warfaremc.tinker.implementation

import eu.warfaremc.tinker.api.TinkerAPI
import eu.warfaremc.tinker.kguava
import eu.warfaremc.tinker.model.Tinkers
import eu.warfaremc.tinker.model.serializable.TinkerData
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.collections.HashSet

class TinkerAPIImplementation : TinkerAPI {

    override fun get(uid: UUID?): Optional<TinkerData> {
        if (uid == null)
            return Optional.empty()

        var result: TinkerData? = kguava.getIfPresent(uid)
            .let { it as TinkerData }

        if (result == null) {
            transaction {
                result = Tinkers.select { Tinkers.uid eq uid.toString() }
                    .firstOrNull().let {
                        if (it == null)
                            null
                        else
                            TinkerData.deserialize(uid, it[Tinkers.data]?.bytes)
                    }
            }
        }

        if (result != null)
            kguava.put(uid, result!!)

        return Optional.ofNullable(result)
    }

    override fun exists(uid: UUID?): Boolean {
        if (uid == null)
            return false
        return get(uid).isPresent //works with cache and is an overall better solution than another transaction
    }

    override fun put(tinkerData: TinkerData?, save: Boolean): Boolean {
        if (tinkerData == null)
            return false

        kguava.put(tinkerData.uid, tinkerData)
        if (save) {
            transaction {
                val serialized = TinkerData.serialize(tinkerData) ?: return@transaction false
                Tinkers.insert {
                    it[Tinkers.uid] = tinkerData.uid.toString()
                    it[Tinkers.data] = ExposedBlob(serialized)
                }
            }
        }

        return true
    }

    override fun getAll(): Set<TinkerData> {
        val set = HashSet<TinkerData>()
        transaction {
            Tinkers.selectAll().map {
                val entry =
                    TinkerData.deserialize(UUID.fromString(it[Tinkers.uid]), it[Tinkers.data]?.bytes) ?: return@map
                set.add(entry)
            }
        }

        return set
    }

}