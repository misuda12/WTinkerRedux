package eu.warfaremc.tinker.implementation

import eu.warfaremc.tinker.api.TinkerAPI
import eu.warfaremc.tinker.kguava
import eu.warfaremc.tinker.model.TinkerData
import eu.warfaremc.tinker.model.Tinkers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.collections.HashSet

class TinkerAPIImplementation : TinkerAPI {

    override fun get(uid: UUID?): Optional<TinkerData> {
        if(uid == null)
            return Optional.empty();

        var result: TinkerData? = kguava.getIfPresent(uid)
            .let { it as TinkerData }

        if(result == null)
        {
            transaction {
                result =  Tinkers.select {Tinkers.uid eq uid.toString()}
                    .firstOrNull().let {
                        if(it == null)
                            null
                        else
                            TinkerData(UUID.fromString(it[Tinkers.uid]), it[Tinkers.data])
                    }
            }
        }

        if(result != null)
            kguava.put(uid, result!!)

        return Optional.ofNullable(result);
    }

    override fun exists(uid: UUID?): Boolean {
        if(uid == null)
            return false
        return get(uid).isPresent //works with cache and is an overall better solution than another transaction
    }

    override fun put(datum: TinkerData?, save: Boolean): Boolean {
        if(datum == null)
            return false

        kguava.put(datum.uid, datum);
        if(save)
            transaction {
                Tinkers.insert {
                    it[uid] = datum.uid.toString()
                    it[data] = datum.data
                }
            }

        return true;
    }

    override fun getAll(): Set<TinkerData> {
        val set =  HashSet<TinkerData>();
        transaction {
            Tinkers.selectAll().map {
                set.add(
                    TinkerData(
                        UUID.fromString(it[Tinkers.uid]), it[Tinkers.data]
                    )
                )
            }
        }

        return set
    }

}