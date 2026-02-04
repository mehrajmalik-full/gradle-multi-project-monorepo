/*
 * This file configures the Gradle build using composite builds.
 *
 * Using composite builds allows each module to be independently buildable
 * while still being part of the monorepo.
 */

rootProject.name = "gradle-multi-project-monorepo"

// Include only selected modules for testing selective loading
includeBuild("libs/greeter")
includeBuild("apps/account-app")

// Commented out other modules to exclude them from the build
// includeBuild("libs/profile")
// includeBuild("apps/inventory-app")
