/*
 * This file configures the inventory-app application using Java 21.
 */

plugins {
    // Apply the java plugin for Java support
    java

    // Apply the application plugin to add support for building a CLI application
    application

    // Create shadow jar
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.example.apps"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // Dependencies on library modules (using composite build)
    implementation("com.example.libs:profile:1.0.0")

    // Use JUnit Jupiter for testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}

application {
    // Define the main class for the application
    mainClass.set("inventory.InventoryApp")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}



