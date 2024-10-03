import net.fabricmc.loom.api.LoomGradleExtensionAPI
import java.io.ByteArrayOutputStream

plugins {
    java
    `maven-publish`
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin") version "0.3.4" // https://github.com/modmuss50/mod-publish-plugin
    id("dev.ithundxr.silk") version "0.11.15" // https://github.com/IThundxr/silk
}

println("ACME Admimn Tools v${"mod_version"()}")

val isRelease = System.getenv("RELEASE_BUILD")?.toBoolean() ?: false
val buildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toInt()
val gitHash = "\"${calculateGitHash() + (if (hasUnstaged()) "-modified" else "")}\""

base.archivesName.set("archives_base_name"())
group = "maven_group"()

// Formats the mod version to include the Mincraft version and build number (if present)
val build = buildNumber?.let { "-build.${it}" } ?: "-local"

version = "${"mod_version"()}+${project.name}-mc${"minecraft_version"() + if (isRelease) "" else build}"

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
}

repositories {
    mavenCentral()
    exclusiveMaven("https://maven.parchmentmc.org", "org.parchmentmc.data") // Parchment mappings
    exclusiveMaven("https://maven.quiltmc.org/repository/release", "org.quiltmc") // Quilt Mappings
}

val loom = project.extensions.getByType<LoomGradleExtensionAPI>()
loom.apply {
    runs.configureEach {
        vmArg("-XX:+AllowEnhancedClassRedefinition")
        vmArg("-XX:+IgnoreUnrecognizedVMOptions")
        vmArg("-Dmixin.debug.export=true")
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}

fun calculateGitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

fun hasUnstaged(): Boolean {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "status", "--porcelain")
        standardOutput = stdout
    }
    val result = stdout.toString().replace("M gradlew", "").trimEnd()
    if (result.isNotEmpty())
        println("Found stageable results:\n${result}\n")
    return result.isNotEmpty()
}

fun RepositoryHandler.exclusiveMaven(url: String, vararg groups: String) {
    exclusiveContent {
        forRepository { maven(url) }
        filter {
            groups.forEach {
                includeGroup(it)
            }
        }
    }
}