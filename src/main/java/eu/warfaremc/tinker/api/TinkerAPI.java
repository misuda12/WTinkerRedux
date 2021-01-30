package eu.warfaremc.tinker.api;

import eu.warfaremc.tinker.model.TinkerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TinkerAPI {

    /**
     * Fetches the result from the tinker database based on the specified {@link UUID}.
     *
     * @param uid {@link UUID} an identifier unique to the tinker tool
     * @return {@link Optional< TinkerData >} database query result
     */
    @NotNull
    Optional<TinkerData> get(@Nullable UUID uid);

    /**
     * Used to check whether the tinker database contains an entry with specified {@link UUID}.
     *
     * @param uid {@link UUID} an identifier unique to the tinker tool
     * @return boolean true if database contains an entry with specified unique identifier false otherwise
     */
    boolean exists(@Nullable UUID uid);

    /**
     * Puts a tinker data object into the tinker database.
     * If you expect rapid changes in succession to this same entry, set the save parameter to false to increase performance.
     *
     * @param data {@link TinkerData} the entry to be put into the tinker database
     * @param save boolean set to true if this entry should be directly put into the database, or cached for later insert
     * @return boolean representing the result of the operation.
     */
    boolean put(@Nullable TinkerData data, boolean save);

    /**
     * Fetches all entries from tinker database.
     *
     * @return {@link Set< TinkerData >} the result
     */
    @NotNull
    Set<TinkerData> getAll();
}
