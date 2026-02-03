/*
 * This file configures the Gradle build using composite builds.
 *
 * Using composite builds allows each module to be independently buildable
 * while still being part of the monorepo.
 */

rootProject.name = "gradle-multi-project-monorepo"

// Include library modules as composite builds
includeBuild("libs/greeter")
includeBuild("libs/profile")

// Include application modules as composite builds
includeBuild("apps/account-app")
includeBuild("apps/inventory-app")

