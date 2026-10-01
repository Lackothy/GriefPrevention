plugins {
    java
}

group = "me.ryanhamshire"
version = "13.9.1"

repositories {
    mavenCentral()
    maven("https://repo.hpfxd.com/releases")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    // PandaSpigot 1.8.8, a superset of the Spigot 1.8.8 API; it also brings
    // Guava and json-simple, which this plugin uses directly.
    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
    // Soft dependencies, all optional at runtime. Not transitive: their own
    // dependency trees reach for repositories that no longer exist.
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { isTransitive = false }
    compileOnly("com.sk89q:worldguard:6.1") { isTransitive = false }
    compileOnly("com.sk89q.worldedit:worldedit-core:6.1") { isTransitive = false }
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:6.1") { isTransitive = false }
    compileOnly("me.clip:placeholderapi:2.11.6") { isTransitive = false }

    testImplementation("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(8)
    options.compilerArgs.addAll(listOf("-Xlint:-options", "-nowarn"))
}

// ── Git info for plugin.yml ──

fun git(vararg args: String): String = try {
    val proc = ProcessBuilder("git", *args).directory(projectDir).start()
    proc.inputStream.bufferedReader().readText().trim()
} catch (_: Exception) {
    ""
}

val gitCommitId: String by lazy { git("rev-parse", "--short=8", "HEAD").ifBlank { "unknown" } }
val gitBranch: String by lazy { git("rev-parse", "--abbrev-ref", "HEAD").let { if (it.isEmpty() || it == "HEAD") "detached" else it } }
val gitStatus: String by lazy { if (git("status", "--porcelain").isBlank()) "clean" else "dirty" }

tasks.processResources {
    filteringCharset = "UTF-8"
    inputs.property("gitCommitId", gitCommitId)
    inputs.property("gitStatus", gitStatus)
    filesMatching("plugin.yml") {
        filter { line ->
            line
                .replace("\${git.commit.id.abbrev}", gitCommitId)
                .replace("\${git.status}", gitStatus)
        }
    }
}

tasks.jar {
    archiveFileName.set("GriefPrevention-${gitBranch}-${gitCommitId}-${gitStatus}.jar")
    destinationDirectory.set(layout.projectDirectory.dir("jars"))
}
