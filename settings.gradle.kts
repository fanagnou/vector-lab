// settings.gradle.kts is read first: it names the build and lists its projects.
// One project here (single module); a multi-module build would add include("core", "bench"), etc.

plugins {
    // If no local JDK matches the toolchain version in build.gradle.kts,
    // Gradle downloads one (via the foojay API) instead of failing.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "vector-lab"
