package eu.warfaremc.tinker.model.extension

val Boolean.intValue
    get() = if (this) 1 else 0
