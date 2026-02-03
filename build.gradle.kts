/*
 * This file configures the root project for a Java-based multi-project monorepo.
 * Using Java 21 with Gradle composite builds.
 *
 * Note: Each module (apps and libs) is configured as a separate composite build
 * and has its own build.gradle.kts and settings.gradle.kts file.
 */

allprojects {
    repositories {
        // Use Maven Central for resolving dependencies
        mavenCentral()
    }
}

