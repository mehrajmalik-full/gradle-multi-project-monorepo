/*
 * Settings file for the account-app application.
 * This defines the composite build dependencies.
 */

rootProject.name = "account-app"

// Include library modules as composite builds
includeBuild("../../libs/greeter")
includeBuild("../../libs/profile")
