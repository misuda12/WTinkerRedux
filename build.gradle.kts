val pluginApiVersion:      String by project
val kotlinApiVersion:      String by project
val javaJvmTarget:         String by project
val kotlinJvmTarget:       String by project
val kotlinVersion:         String by project
val kotlinLanguageVersion: String by project
val spigotApiVersion:      String by project

plugins {
    java
    idea
    kotlin("jvm")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "kotlin")
    kotlin {
        explicitApi()
        sourceSets.all {
            with(languageSettings) {
                apiVersion = kotlinApiVersion
                languageVersion = kotlinLanguageVersion
                progressiveMode = true
                enableLanguageFeature("InlineClasses")
                useExperimentalAnnotation("kotlin.RequiresOptIn")
                useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
            }
        }
    }

    group       = "eu.warfaremc.tinker"
    version     = pluginApiVersion
    description = "This addon implements multiple items into Slimefun4"

    repositories {
        jcenter()
        mavenCentral()
        maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
        maven(url = "https://dl.bintray.com/kotlin/dokka/")
        maven(url = "https://dl.bintray.com/jetbrains/markdown/")
        maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven(url = "https://oss.sonatype.org/content/groups/public/")
        maven(url = "https://jitpack.io")

        mavenLocal()

        flatDir {
            dirs("libs")
        }
    }

    dependencies {
        api("io.github.microutils:kotlin-logging-jvm:2.0.6") {
            exclude(group = "org.jetbrains.kotlin")
        }
        compileOnly("org.spigotmc:spigot-api:$spigotApiVersion")
    }

    tasks.compileJava.get().options.encoding = "UTF-8"
    tasks.compileJava.get().options.release.set(javaJvmTarget.toInt())

    tasks.compileKotlin.get().kotlinOptions.jvmTarget = kotlinJvmTarget
}

subprojects {
    val project            = this@subprojects
    if (name == "runtime") return@subprojects
    tasks {
        val sourcesJar by creating(Jar::class) {
            from(sourceSets["main"].allSource)
            archiveClassifier.set("sources")
        }
    }
}