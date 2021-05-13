plugins { id("com.github.johnrengelman.shadow") }
base.archivesBaseName = "WTinkerRedux"

dependencies {
    api(project(":api"))
    api(project(":common"))
}

tasks {
    artifacts.archives(shadowJar)
    processResources.get().expand(
        "pluginName"        to "WTinkerRedux",
        "rootName"          to parent?.name,
        "pluginDescription" to description,
        "pluginApiVersion"  to version
    )
    shadowJar.get().relocate("org.slf4j", "shadowed.org.slf4j")
}