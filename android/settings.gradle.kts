pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

configureGradleJdk(rootDir)

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FriendZone"
include(":app")

fun configureGradleJdk(rootDir: java.io.File) {
    val localPropertiesFile = rootDir.resolve("local.properties")
    val props = java.util.Properties()
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { props.load(it) }
    }

    val resolved = props.getProperty("org.gradle.java.home") ?: detectMacJdkHome()
    if (resolved.isNullOrBlank()) return

    val javaHome = java.io.File(resolved)
    if (!java.io.File(javaHome, "bin/jlink").canExecute()) return

    val current = System.getProperty("org.gradle.java.home")
    val unusable = current.isNullOrBlank() ||
        current.contains(".cursor/extensions") ||
        current.contains(".vscode/extensions") ||
        !java.io.File(current, "bin/jlink").canExecute()

    if (unusable || current != javaHome.absolutePath) {
        System.setProperty("org.gradle.java.home", javaHome.absolutePath)
    }
}

fun detectMacJdkHome(): String? {
    if (!System.getProperty("os.name").contains("Mac", ignoreCase = true)) return null
    for (version in listOf("21", "17", "22", "11")) {
        val process = ProcessBuilder("/usr/libexec/java_home", "-v", version)
            .redirectErrorStream(true)
            .start()
        val home = process.inputStream.bufferedReader().readText().trim()
        if (process.waitFor() == 0 && java.io.File(home, "bin/jlink").canExecute()) {
            return home
        }
    }
    return null
}
 