import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import wtf.gofancy.fancygradle.script.extensions.createDataGenerationRunConfig
import wtf.gofancy.fancygradle.script.extensions.createDebugLoggingRunConfig
import wtf.gofancy.fancygradle.script.extensions.curseForge

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") version "1.5.21"

    id("com.github.ben-manes.versions") version "0.39.0"
    id("net.minecraftforge.gradle") version "5.1.22"
    id("org.cadixdev.licenser") version "0.6.1"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("org.parchmentmc.librarian.forgegradle") version "1.1.3"
    id("wtf.gofancy.fancygradle") version "1.0.1"
}

val gitCommit: String
    get() = try {
        val process = ProcessBuilder("git rev-parse --short HEAD").start()
        val input = process.inputStream
        val lines = BufferedReader(InputStreamReader(input)).lines()
        if (process.waitFor() != 0) "Error while querying Git" else lines.reduce { t, u -> t + u }.get()
    } catch (e: IOException) {
        "Error while looking up Git: $e"
    }

val buildNumber: String? get() = System.getProperty("BUILD_NUMBER")
val buildString: String get() = buildNumber?.let { "+build.$it" } ?: ""

val modId = "squared_crafting"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_1_8.majorVersion))
}

ktlint {
    version.set("0.41.0")
    debug.set(true)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    enableExperimentalRules.set(true)
    disabledRules.set(setOf("indent"))
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
}

@Suppress("SpellCheckingInspection")
license {
    header(project.file("NOTICE"))

    properties {
        this["app"] = "Squared Crafting"
        this["year"] = DateTimeFormatter.ofPattern("uuuu").withZone(ZoneId.from(ZoneOffset.UTC)).format(Instant.now())
        this["author"] = "TheSilkMiner"
        this["email"] = "thesilkminer <at> outlook <dot> com"
    }
}

minecraft {
    mappings("parchment", "2021.08.29-1.16.5")

    runs {
        createDebugLoggingRunConfig("client", modId, sourceSets.main.get())
        createDebugLoggingRunConfig("server", modId, sourceSets.main.get()) { args("nogui") }
        createDataGenerationRunConfig("data", modId, sourceSets.main.get())
    }
}

group = "net.thesilkminer.mc"
version = "1.0.0$buildString"

repositories {
    mavenCentral()
    curseForge()
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge")
    }
}

dependencies {
    minecraft(group = "net.minecraftforge", name = "forge", version = "1.16.5-36.2.4")

    implementation(group = "thedarkcolour", name = "kotlinforforge", version = "1.14.0")
}

sourceSets {
    main.get().resources.srcDir("src/generated/resources")
}

tasks {
    withType<Jar> {
        manifest {
            attributes(
                "Name" to "Squared Crafting",
                "Specification-Title" to project.name,
                "Specification-Version" to project.version,
                "Specification-Vendor" to "TheSilkMiner",
                "Implementation-Title" to "${project.group}.${project.name}",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "TheSilkMiner",
                "Implementation-Timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                "Git-Commit" to gitCommit,
                "Automatic-Module-Name" to "net.thesilkminer.mc.squaredcrafting"
            )
        }
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = listOf("-Xjvm-default=all", "-Xlambdas=indy")
        }
    }

    withType<Wrapper> {
        gradleVersion = "7.1.1"
        distributionType = Wrapper.DistributionType.ALL
    }
}

