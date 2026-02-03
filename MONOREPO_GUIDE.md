# Gradle Monorepo Guide

This guide explains our Gradle monorepo setup, structure, CI/CD configuration, and how to work with it efficiently.

---

## What is a Monorepo?

A monorepo is a single repository containing multiple projects, applications, and libraries. Instead of managing 15+ separate repositories, we consolidate them into one.

### Why Monorepos?

**Benefits:**
- Single place for all Java projects
- Shared code changes happen in one place
- Consistent tooling and dependencies across projects
- Easier refactoring across module boundaries
- Simplified version control and code reviews

**Our Implementation:**
We use **Gradle Composite Builds** - each module is an independent Gradle project that can be built standalone, but they're all in one repository.

---

## Project Structure

Our monorepo organizes code by type:

```
gradle-multi-project-monorepo/
├── apps/               # Applications (services)
│   ├── account-app/
│   └── inventory-app/
├── libs/               # Shared libraries
│   ├── greeter/
│   └── profile/
├── settings.gradle.kts # Root: includes all modules
└── .github/workflows/  # CI/CD pipelines
```

### Module Organization

Each module is a complete Gradle project:

```
apps/account-app/
├── settings.gradle.kts  # Declares dependencies on other modules
├── build.gradle.kts     # Build configuration (Java 21, plugins, deps)
└── src/
    ├── main/java/       # Application code
    └── test/java/       # Tests
```

### Dependency Structure

```
account-app (apps/account-app)
├── greeter (libs/greeter)
└── profile (libs/profile)

inventory-app (apps/inventory-app)
└── profile (libs/profile)
```

---

## How Composite Builds Work

### Root Configuration

`settings.gradle.kts` includes all modules as composite builds:

```kotlin
rootProject.name = "gradle-multi-project-monorepo"

// Include library modules
includeBuild("libs/greeter")
includeBuild("libs/profile")

// Include application modules
includeBuild("apps/account-app")
includeBuild("apps/inventory-app")
```

### Module Dependencies

Each module declares its own dependencies in `settings.gradle.kts`:

```kotlin
// apps/account-app/settings.gradle.kts
rootProject.name = "account-app"

includeBuild("../../libs/greeter")
includeBuild("../../libs/profile")
```

Then references them in `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.example.libs:greeter:1.0.0")
    implementation("com.example.libs:profile:1.0.0")
}
```

### Why This Approach?

- Each module can be built independently
- Gradle automatically resolves composite dependencies
- Clear module boundaries
- Easy to extract a module to its own repo later
- Better IDE support

---

## Building and Running

### Build a Specific Module

```bash
cd apps/account-app
../../gradlew build
```

When you build a module, Gradle automatically:
1. Detects dependencies on other modules
2. Builds those dependencies first
3. Uses the latest local code (not artifacts)

### Run an Application

```bash
cd apps/account-app
../../gradlew run
# Output: [account-service]: Hi, Alice. Hello world from Greeter.
```

### Create Deployable JAR

```bash
cd apps/account-app
../../gradlew shadowJar
java -jar build/libs/account-app-1.0.0-all.jar
```

---

## CI/CD with GitHub Actions

### Targeted Builds

Each application has its own workflow that triggers only when relevant files change:

```yaml
# .github/workflows/build-account-app.yml
name: Build account-app
on:
  push:
    paths:
      - "apps/account-app/**"    # App code changed
      - "libs/greeter/**"        # Dependency changed
      - "libs/profile/**"        # Dependency changed
      - "*.gradle.kts"           # Build config changed
      - "gradle.properties"
      - "gradle/wrapper/**"
```

**Result:** Only affected applications are built when code changes.

Example:
- Change to `libs/profile` → triggers `account-app` and `inventory-app` builds
- Change to `libs/greeter` → triggers only `account-app` build
- Change to `apps/account-app` → triggers only `account-app` build

### Multi-Layer Caching

We use three layers of caching for fast builds:

#### 1. Dependency Cache (via `actions/setup-java`)

```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'corretto'
    cache: 'gradle'  # Caches downloaded JARs
```

Caches:
- Maven dependencies (~50 MB)
- Gradle wrapper distribution (~110 MB)

#### 2. Build Cache (via `gradle/gradle-build-action`)

```yaml
- name: Setup Gradle
  uses: gradle/gradle-build-action@v3
  with:
    cache-read-only: ${{ github.ref != 'refs/heads/main' }}
    gradle-home-cache-cleanup: true
```

Caches:
- Compiled classes
- Test results
- Task outputs

**Cache Strategy:**
- `main` branch: Read-write (updates cache)
- Feature branches: Read-only (uses cache, doesn't update)

This prevents feature branches from polluting the cache.

#### 3. Task Output Cache (via `--build-cache`)

```yaml
- name: Build account-app
  run: cd apps/account-app && ../../gradlew build --no-daemon --build-cache
```

Reuses task outputs across builds when inputs haven't changed.

### Build Performance

**First Build (no cache):**
- Download Gradle: 30s
- Download dependencies: 15s
- Compile + test: 8s
- **Total: ~53 seconds**

**Subsequent Builds (with cache):**
- Restore caches: 5s
- Compile (if changed): 2s
- Test (if changed): 3s
- **Total: ~10 seconds (5x faster)**

**No changes:**
- Restore caches: 5s
- All tasks: UP-TO-DATE
- **Total: ~5 seconds (10x faster)**

### Cache Configuration

Gradle is configured for optimal caching in `libs/greeter/build.gradle.kts`:

```kotlin
// Make JAR task reproducible for build cache
tasks.withType<Jar> {
    // Preserve file timestamps for reproducibility
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
```

This ensures the same inputs always produce the same output, maximizing cache hits.

---

## IDE Module Management

### IntelliJ IDEA

IntelliJ automatically detects composite builds and loads all modules by default.

#### View Loaded Modules

The `.idea/gradle.xml` file shows the composite structure:

```xml
<compositeBuild compositeDefinitionSource="SCRIPT">
  <builds>
    <build path="$PROJECT_DIR$/apps/account-app" name="account-app" />
    <build path="$PROJECT_DIR$/libs/greeter" name="greeter" />
    <build path="$PROJECT_DIR$/apps/inventory-app" name="inventory-app" />
    <build path="$PROJECT_DIR$/libs/profile" name="profile" />
  </builds>
</compositeBuild>
```

#### Unload Modules You Don't Need

To speed up indexing and reduce memory usage:

1. **Open Gradle Tool Window** (View → Tool Windows → Gradle)
2. **Right-click on a module** (e.g., `inventory-app`)
3. **Select "Unload Gradle Project"**

This excludes it from indexing and compilation.

#### Reload a Module

1. **Open Gradle Tool Window**
2. **Right-click on the module**
3. **Select "Load Gradle Project"**

#### Work on a Specific Module

If you're only working on `account-app` and `greeter`:

1. Unload `inventory-app`
2. Unload `profile` (if not needed)
3. Refresh Gradle (click the refresh icon)

**Result:** Faster indexing, less memory usage, cleaner IDE.

#### Alternative: Open Module Directly

Instead of opening the root, open a specific module:

```bash
# Open only account-app in IntelliJ
idea apps/account-app
```

IntelliJ will still detect and load dependencies (`greeter`, `profile`) via composite builds.

### VS Code

1. **Install Extensions:**
   - Java Extension Pack
   - Gradle for Java

2. **Open Root Directory:**
   VS Code loads the full project.

3. **Work with Specific Modules:**
   Use workspace folders to focus on specific modules:
   - File → Add Folder to Workspace → Select `apps/account-app`
   - Remove root folder if needed

---

## Code Examples

### Example 1: Adding a New Library

Create `libs/utils`:

```bash
mkdir -p libs/utils/src/main/java/utils
cd libs/utils
```

Create `settings.gradle.kts`:
```kotlin
rootProject.name = "utils"
```

Create `build.gradle.kts`:
```kotlin
plugins {
    `java-library`
}

group = "com.example.libs"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

Include in root `settings.gradle.kts`:
```kotlin
includeBuild("libs/utils")
```

### Example 2: Using the New Library

In `apps/account-app/settings.gradle.kts`:
```kotlin
includeBuild("../../libs/utils")
```

In `apps/account-app/build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.example.libs:utils:1.0.0")
    // other deps...
}
```

In Java code:
```java
import utils.SomeUtilClass;

public class AccountApp {
    public void doSomething() {
        SomeUtilClass.doWork();
    }
}
```

### Example 3: Module Communication

From `apps/account-app/src/main/java/account/AccountApp.java`:

```java
package account;

import greeter.Greeter;      // From libs/greeter
import profile.Profile;      // From libs/profile

public class AccountApp {
    public String getGreeting() {
        String profile = new Profile().getCurrentProfile();
        return "Hi, " + profile + ". " + new Greeter().getGreeting();
    }

    public static void main(String[] args) {
        System.out.println("[account-service]: " + new AccountApp().getGreeting());
    }
}
```

When you run this:
1. Gradle builds `greeter` and `profile` first
2. Compiles `account-app` with those dependencies
3. Runs the application

### Example 4: Testing with Dependencies

From `apps/account-app/src/test/java/account/AccountAppTest.java`:

```java
package account;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountAppTest {
    @Test
    void testGreeting() {
        AccountApp app = new AccountApp();
        String greeting = app.getGreeting();
        
        // Tests integration with both greeter and profile libs
        assertTrue(greeting.contains("Alice"));
        assertTrue(greeting.contains("Hello world"));
    }
}
```

Run tests:
```bash
cd apps/account-app
../../gradlew test
```

Gradle automatically rebuilds `greeter` and `profile` if their code changed.

---

## Common Workflows

### Adding a New Service

1. Create directory: `apps/my-service`
2. Add `settings.gradle.kts` with dependencies
3. Add `build.gradle.kts` with configuration
4. Create source code
5. Include in root `settings.gradle.kts`:
   ```kotlin
   includeBuild("apps/my-service")
   ```
6. Create CI workflow: `.github/workflows/build-my-service.yml`

### Changing a Shared Library

1. Make changes to `libs/profile`
2. Build and test locally:
   ```bash
   cd libs/profile
   ../../gradlew build test
   ```
3. Test affected applications:
   ```bash
   cd ../../apps/account-app
   ../../gradlew test
   
   cd ../inventory-app
   ../../gradlew test
   ```
4. Push changes
5. CI automatically builds `account-app` and `inventory-app`

### Refactoring Across Modules

Since all code is in one repo:

1. Use IDE refactoring (Rename, Move, etc.)
2. Changes apply across all modules
3. Single commit contains all related changes
4. Single PR for the entire change

---

## Best Practices

### Module Design

- Keep modules small and focused
- Use `java-library` plugin for libraries
- Use `application` plugin for services
- Define clear interfaces between modules

### Versioning

- Each module has its own version
- Increment version when you change a module
- Dependent modules reference specific versions

### CI/CD

- One workflow per application
- Path-based triggers to avoid unnecessary builds
- Use caching to speed up builds
- Keep workflows simple and focused

### IDE Usage

- Unload modules you're not working on
- Reload when needed
- Use workspace features to focus on specific areas

---

## Troubleshooting

### Build Issues

**Problem:** Circular dependencies

**Solution:** Check `settings.gradle.kts` files - ensure dependencies are one-way

**Problem:** Dependency not found

**Solution:** 
1. Check `group`, `version` in the library's `build.gradle.kts`
2. Ensure the library is included in the consuming module's `settings.gradle.kts`
3. Refresh Gradle: `../../gradlew --refresh-dependencies`

### IDE Issues

**Problem:** IDE doesn't see module changes

**Solution:**
1. Invalidate caches: File → Invalidate Caches
2. Reload Gradle: Gradle Tool Window → Reload

**Problem:** Too slow to index

**Solution:**
1. Unload unused modules
2. Increase IDE memory: Help → Edit Custom VM Options → Increase `-Xmx`
3. Exclude build directories: Preferences → Project → Ignored Files

### CI Issues

**Problem:** Build not triggered

**Solution:** Check the `paths` filter in workflow - ensure changed files match

**Problem:** Build too slow

**Solution:** Check cache hit rate in logs - may need to adjust cache configuration

---

## Summary

Our Gradle monorepo uses composite builds to:
- Keep 15+ Java projects in one place
- Enable independent module builds
- Provide targeted CI/CD with smart caching
- Support flexible IDE workflows
- Maintain clear module boundaries

Key files:
- `settings.gradle.kts` (root): Lists all modules
- `settings.gradle.kts` (module): Declares dependencies
- `build.gradle.kts` (module): Build configuration
- `.github/workflows/*.yml`: CI/CD pipelines

This setup scales well and provides a good developer experience for teams working on multiple related Java services.
