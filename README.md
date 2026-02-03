# Gradle Multi-Project Monorepo with Composite Builds

This repository demonstrates a Gradle-based multi-project monorepo using **Java 21** and **Gradle Composite Builds**.

## 📖 Documentation

**[→ Read the Complete Monorepo Guide](MONOREPO_GUIDE.md)** - Covers everything you need to know about working with this monorepo, including CI/CD, caching, and IDE configuration.

## 🏗️ Project Structure

This monorepo contains:
- **2 Library Modules**: `libs/greeter`, `libs/profile`
- **2 Application Modules**: `apps/account-app`, `apps/inventory-app`

### Dependency Graph

```
apps/account-app
├── libs/greeter
└── libs/profile

apps/inventory-app
└── libs/profile
```

Both apps depend on `libs/profile`, and only `apps/account-app` depends on `libs/greeter`.

## 🚀 Technology Stack

- **Java**: 21 (with Java toolchain support)
- **Gradle**: 8.5
- **Build Architecture**: Gradle Composite Builds
- **Testing**: JUnit 5 (Jupiter)
- **Packaging**: Shadow plugin for creating fat JARs

## 📦 Composite Builds

This project uses Gradle's **composite builds** feature via the `includeBuild()` function. Each module is a separate Gradle project with its own `settings.gradle.kts` file.

### Benefits of Composite Builds:
- ✅ Each module can be built independently
- ✅ Faster incremental builds
- ✅ Better IDE support
- ✅ Clear dependency boundaries
- ✅ Easier to extract modules into separate repositories
- ✅ No parent project coupling

### How It Works:

1. **Root `settings.gradle.kts`** includes all modules:
   ```kotlin
   includeBuild("libs/greeter")
   includeBuild("libs/profile")
   includeBuild("apps/account-app")
   includeBuild("apps/inventory-app")
   ```

2. **Each module's `settings.gradle.kts`** declares its composite dependencies:
   ```kotlin
   // apps/account-app/settings.gradle.kts
   rootProject.name = "account-app"
   includeBuild("../../libs/greeter")
   includeBuild("../../libs/profile")
   ```

3. **Dependencies use group:name:version format**:
   ```kotlin
   dependencies {
       implementation("com.example.libs:greeter:1.0.0")
       implementation("com.example.libs:profile:1.0.0")
   }
   ```

## 🛠️ Building the Project

### Build All Modules

From any module directory:
```bash
# Build a specific library
cd libs/greeter
../../gradlew build

# Build a specific app
cd apps/account-app
../../gradlew build
```

### Run Applications

```bash
# Run account-app
cd apps/account-app
../../gradlew run
# Output: [account-service]: Hi, Alice. Hello world from Greeter.

# Run inventory-app
cd apps/inventory-app
../../gradlew run
# Output: [inventory-app] Hi, Alice.
```

### Run Tests

```bash
# Test a specific module
cd libs/greeter
../../gradlew test

# Test an app with its dependencies
cd apps/account-app
../../gradlew test
```

### Create Fat JARs

```bash
cd apps/account-app
../../gradlew shadowJar
# Creates: build/libs/account-app-1.0.0-all.jar

java -jar build/libs/account-app-1.0.0-all.jar
```

## 📁 Module Structure

Each module follows this structure:

```
module-name/
├── build.gradle.kts          # Build configuration
├── settings.gradle.kts       # Composite build dependencies
└── src/
    ├── main/java/           # Java source files
    └── test/java/           # Test files
```

## 🔧 Java 21 Features

This project is configured to use Java 21 with Gradle's toolchain support:

```kotlin
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

This ensures:
- Consistent Java version across all modules
- Automatic JDK provisioning if needed
- Build reproducibility

## 📝 Module Details

### Libraries

#### `libs/greeter`
- **Group**: `com.example.libs`
- **Artifact**: `greeter`
- **Version**: `1.0.0`
- **Purpose**: Provides greeting functionality

#### `libs/profile`
- **Group**: `com.example.libs`
- **Artifact**: `profile`
- **Version**: `1.0.0`
- **Purpose**: Provides user profile functionality

### Applications

#### `apps/account-app`
- **Group**: `com.example.apps`
- **Artifact**: `account-app`
- **Version**: `1.0.0`
- **Main Class**: `account.AccountApp`
- **Dependencies**: greeter, profile

#### `apps/inventory-app`
- **Group**: `com.example.apps`
- **Artifact**: `inventory-app`
- **Version**: `1.0.0`
- **Main Class**: `inventory.InventoryApp`
- **Dependencies**: profile

## 🎯 Use Cases

This architecture is ideal for:
- Microservices sharing common libraries
- Large codebases requiring modular organization
- Projects needing independent module versioning
- Teams wanting to maintain multiple applications in one repository
- Gradual migration from monolith to microservices

## 🔄 Converting from Standard Multi-Project Build

This project was converted from a standard Gradle multi-project build to use composite builds. Key changes:

1. Created `settings.gradle.kts` for each module
2. Changed from `project(":libs:greeter")` to `"com.example.libs:greeter:1.0.0"`
3. Added `group` and `version` to each module's build file
4. Used `includeBuild()` instead of `include()` in root settings

## 📚 Additional Resources

- [Gradle Composite Builds Documentation](https://docs.gradle.org/current/userguide/composite_builds.html)
- [Java Toolchains in Gradle](https://docs.gradle.org/current/userguide/toolchains.html)
- [Shadow Plugin Documentation](https://imperceptiblethoughts.com/shadow/)

## 📄 License

This is an example project for educational purposes.
