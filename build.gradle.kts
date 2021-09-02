import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import wtf.gofancy.fancygradle.script.extensions.createDataGenerationRunConfig
import wtf.gofancy.fancygradle.script.extensions.createDebugLoggingRunConfig
import wtf.gofancy.fancygradle.script.extensions.curseForge
import wtf.gofancy.fancygradle.script.extensions.deobf

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
        if (process.waitFor() != 0) "Unable to query Git" else lines.reduce { t, u -> t + u }.get()
    } catch (e: IOException) {
        "Unable to run Git"
    }

val buildNumber: String? get() = System.getProperty("BUILD_NUMBER")
val buildString: String get() = buildNumber?.let { "+build.$it" } ?: ""

val modId = "squared_crafting"

val generated: SourceSet by sourceSets.creating {
    resources {
        exclude(".cache")
    }
    afterEvaluate {
        compileClasspath += sourceSets.main.get().output
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_1_8.majorVersion))
}

ktlint {
    version.set("0.42.1")
    debug.set(true)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    enableExperimentalRules.set(true)
    disabledRules.set(setOf("indent", "import-ordering"))
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
        createDebugLoggingRunConfig("client", modId, sourceSets.main.get(), generated)
        createDebugLoggingRunConfig("server", modId, sourceSets.main.get(), generated) { args("nogui") }
        createDataGenerationRunConfig("data", modId, sourceSets.main.get(), generated)
    }
}

group = "net.thesilkminer.mc"
version = "1.0.0$buildString"

repositories {
    mavenCentral()
    curseForge()
    maven {
        name = "BlameJared"
        url = uri("https://maven.blamejared.com/")

        content {
            includeGroup("com.blamejared.crafttweaker")
        }
    }
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge")

        content {
            includeGroup("thedarkcolour")
        }
    }
    maven {
        name = "Progwml6"
        url = uri("https://dvs1.progwml6.com/files/maven/")

        content {
            includeGroup("mezz.jei")
        }
    }
    maven {
        name = "ModMaven" // fallback, hence why last
        url = uri("https://modmaven.k-4u.nl")
    }
}

dependencies {
    minecraft(group = "net.minecraftforge", name = "forge", version = "1.16.5-36.2.4")

    annotationProcessor(group = "com.blamejared.crafttweaker", name = "Crafttweaker_Annotation_Processors-1.16.5", version = "1.0.0.414")

    implementation(fg.deobf(group = "com.blamejared.crafttweaker", name = "CraftTweaker-1.16.5", version = "7.1.2.414"))

    compileOnly(fg.deobf(group = "mezz.jei", name = "jei-1.16.5", version = "7.7.1.121", classifier = "api"))
    runtimeOnly(fg.deobf(group = "mezz.jei", name = "jei-1.16.5", version = "7.7.1.121"))

    implementation(group = "thedarkcolour", name = "kotlinforforge", version = "1.14.0")
}

tasks {
    withType<Jar> {
        from(sourceSets.main.get().output, generated.output)
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
        gradleVersion = "7.2"
        distributionType = Wrapper.DistributionType.ALL
    }
}
