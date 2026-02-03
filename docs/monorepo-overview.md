# Gradle Monorepo Overview

This note explains how the repo uses a Gradle monorepo with composite builds, the layout we chose, how CI/CD caching is wired, and how to work with selective modules locally and in IDEs.

## What a Monorepo Is and How We Implemented It
- Single repository hosting multiple Java apps and shared libraries.
- Each module is an independent Gradle build (its own `settings.gradle.kts`) composed via `includeBuild`.
- Dependencies are declared with `group:artifact:version`, letting modules be built and tested alone or together.
- Java 21 toolchain configured in each module to keep versions consistent.

## Folder Structure
```
/gradle-multi-project-monorepo
├── apps/
│   ├── account-app/
│   └── inventory-app/
├── libs/
│   ├── greeter/
│   └── profile/
├── gradle/                 # wrapper files
├── build.gradle.kts        # (root helper if needed)
└── settings.gradle.kts     # selects which modules to load
```

## How Modules Are Wired (Composite Builds)
- Root `settings.gradle.kts` controls which modules participate; unused modules can stay commented to keep builds/indexing light:
```kotlin
rootProject.name = "gradle-multi-project-monorepo"
includeBuild("libs/greeter")
includeBuild("apps/account-app")
// includeBuild("libs/profile")
// includeBuild("apps/inventory-app")
```
- Each app/library declares its own composites. Example `apps/account-app/settings.gradle.kts`:
```kotlin
rootProject.name = "account-app"
includeBuild("../../libs/greeter")
includeBuild("../../libs/profile")
```
- Dependencies reference the published coordinates, enabling targeted builds. From `apps/account-app/build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.example.libs:greeter:1.0.0")
    implementation("com.example.libs:profile:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}
```

## CI/CD and Caching (GitHub Actions)
- Java setup + dependency cache via `actions/setup-java`:
```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'corretto'
    cache: 'gradle'
```
- Gradle build action reuses configuration/build cache and keeps feature branches read-only to avoid pollution:
```yaml
- name: Setup Gradle
  uses: gradle/gradle-build-action@v3
  with:
    cache-read-only: ${{ github.ref != 'refs/heads/main' }}
    gradle-home-cache-cleanup: true
```
- Targeted module build step keeps the cache hot and avoids building everything:
```yaml
- name: Build account-app
  run: cd apps/account-app && ../../gradlew build --no-daemon --build-cache
```
- Cache keys derive from Gradle wrapper + `*.gradle*` files; changes there refresh caches automatically.

## Developer Workflow
- Build a single module: `cd apps/account-app && ../../gradlew build`.
- Build with fat JAR: `cd apps/account-app && ../../gradlew shadowJar`.
- Run tests per module: `cd libs/greeter && ../../gradlew test`.
- Enable/disable modules for local work: toggle `includeBuild` lines in the root `settings.gradle.kts`; Gradle and IDEs will only sync the included set.

## IDE Tips (IntelliJ/VS Code)
- Open the repo root, then in the Gradle tool window refresh after commenting/uncommenting `includeBuild` entries to limit indexing.
- For a lightweight session, open a module directory (e.g., `apps/account-app`) on its own; composite includes pull in only its dependencies.
- If indexing grows, temporarily exclude unused module directories in the IDE or keep them commented in root settings until needed.

## Next Steps / Recommendations
- Add or adjust GitHub Actions workflows to mirror the snippets above and run only the modules touched by path filters.
- If build time grows, consider enabling a remote Gradle build cache (see `GITHUB_ACTIONS_CACHING.md` for properties).
- Keep module versions aligned; release shared libraries first, then apps that consume them.
