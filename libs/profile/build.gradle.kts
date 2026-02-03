/*
 * This file configures the profile library module using Java 21.
 */

plugins {
    // Apply the java-library plugin for API and implementation separation
    `java-library`
}

group = "com.example.libs"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    // Ensure reproducible builds for better caching
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    // Use JUnit Jupiter for testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}

tasks.test {
    useJUnitPlatform()
}

// Make JAR task reproducible for build cache
tasks.withType<Jar> {
    // Preserve file timestamps for reproducibility
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    // Don't include timestamps in manifest
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}


