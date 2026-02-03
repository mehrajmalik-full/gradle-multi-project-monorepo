# Quick Reference Guide

## Common Commands

### Building Modules

#### Build a specific library
```bash
cd libs/greeter
../../gradlew build

cd libs/profile
../../gradlew build
```

#### Build a specific application
```bash
cd apps/account-app
../../gradlew build

cd apps/inventory-app
../../gradlew build
```

### Running Applications

#### Run account-app
```bash
cd apps/account-app
../../gradlew run
# Output: [account-service]: Hi, Alice. Hello world from Greeter.
```

#### Run inventory-app
```bash
cd apps/inventory-app
../../gradlew run
# Output: [inventory-app] Hi, Alice.
```

### Running Tests

#### Test a specific module
```bash
cd libs/greeter
../../gradlew test

cd libs/profile
../../gradlew test

cd apps/account-app
../../gradlew test

cd apps/inventory-app
../../gradlew test
```

#### Test with verbose output
```bash
../../gradlew test --info
```

### Creating Fat JARs

#### Create account-app JAR
```bash
cd apps/account-app
../../gradlew shadowJar
# Output: build/libs/account-app-1.0.0-all.jar

# Run the JAR
java -jar build/libs/account-app-1.0.0-all.jar
```

#### Create inventory-app JAR
```bash
cd apps/inventory-app
../../gradlew shadowJar
# Output: build/libs/inventory-app-1.0.0-all.jar

# Run the JAR
java -jar build/libs/inventory-app-1.0.0-all.jar
```

### Cleaning Build Artifacts

#### Clean a specific module
```bash
cd libs/greeter
../../gradlew clean
```

#### Clean all modules
```bash
cd /path/to/gradle-multi-project-monorepo
rm -rf build apps/*/build libs/*/build
rm -rf .gradle apps/*/.gradle libs/*/.gradle
```

### Gradle Tasks

#### List available tasks
```bash
../../gradlew tasks
```

#### List all tasks including dependencies
```bash
../../gradlew tasks --all
```

#### Show dependency tree
```bash
../../gradlew dependencies
```

### Development Workflow

#### 1. Make changes to a library
```bash
cd libs/greeter
# Edit src/main/java/greeter/Greeter.java
../../gradlew build test
```

#### 2. Test changes in dependent app
```bash
cd ../../apps/account-app
../../gradlew clean build run
```

#### 3. Create a release JAR
```bash
../../gradlew shadowJar
java -jar build/libs/account-app-1.0.0-all.jar
```

### Troubleshooting

#### Check Java version
```bash
java -version
# Should show Java 21
```

#### Check Gradle version
```bash
../../gradlew --version
# Should show Gradle 8.5
```

#### Build with stacktrace
```bash
../../gradlew build --stacktrace
```

#### Build with debug info
```bash
../../gradlew build --debug
```

#### Force refresh dependencies
```bash
../../gradlew build --refresh-dependencies
```

#### Clean and rebuild everything
```bash
cd libs/greeter && ../../gradlew clean build
cd ../profile && ../../gradlew clean build
cd ../../apps/account-app && ../../gradlew clean build
cd ../inventory-app && ../../gradlew clean build
```

## Project Structure Quick View

```
gradle-multi-project-monorepo/
├── settings.gradle.kts          # Root settings with includeBuild()
├── build.gradle.kts             # Root build configuration
├── gradle/wrapper/              # Gradle wrapper (v8.5)
├── libs/
│   ├── greeter/
│   │   ├── settings.gradle.kts  # Module settings
│   │   ├── build.gradle.kts     # Module build config
│   │   └── src/
│   │       ├── main/java/       # Java source
│   │       └── test/java/       # Test source
│   └── profile/
│       ├── settings.gradle.kts
│       ├── build.gradle.kts
│       └── src/
│           ├── main/java/
│           └── test/java/
└── apps/
    ├── account-app/
    │   ├── settings.gradle.kts  # Declares composite dependencies
    │   ├── build.gradle.kts
    │   └── src/
    │       ├── main/java/
    │       └── test/java/
    └── inventory-app/
        ├── settings.gradle.kts
        ├── build.gradle.kts
        └── src/
            ├── main/java/
            └── test/java/
```

## Dependency Graph

```
account-app depends on:
  ├── greeter (com.example.libs:greeter:1.0.0)
  └── profile (com.example.libs:profile:1.0.0)

inventory-app depends on:
  └── profile (com.example.libs:profile:1.0.0)
```

## Quick Checks

### ✅ Verify Everything Works
```bash
# From any module directory
cd libs/greeter && ../../gradlew clean test && echo "✅ greeter OK"
cd ../profile && ../../gradlew clean test && echo "✅ profile OK"
cd ../../apps/account-app && ../../gradlew clean test && echo "✅ account-app OK"
cd ../inventory-app && ../../gradlew clean test && echo "✅ inventory-app OK"
```

### ✅ Run All Apps
```bash
cd apps/account-app && ../../gradlew run
cd ../inventory-app && ../../gradlew run
```

## Tips

1. **Always build from the module directory**, not the root
2. **Use `--no-daemon`** for CI/CD to avoid daemon issues
3. **Use `-q` or `--quiet`** for less verbose output
4. **Composite builds resolve automatically** - no need to build dependencies first
5. **Each module is independent** - can be extracted to its own repo easily

## Environment Requirements

- **Java**: 21 or higher
- **Gradle**: 8.5 (managed by wrapper)
- **OS**: Any (Linux, macOS, Windows)

## IDE Setup

### IntelliJ IDEA
1. Open the root directory
2. IDEA will auto-detect the Gradle structure
3. Enable "Use Gradle from specified location" in settings
4. Set Project SDK to Java 21

### VS Code
1. Install Java Extension Pack
2. Open the root directory
3. Gradle tasks will be available in the sidebar

### Eclipse
1. Install Buildship Gradle plugin
2. Import as "Existing Gradle Project"
3. Set JDK to Java 21
