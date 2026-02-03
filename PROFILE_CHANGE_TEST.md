# Verification: Profile Change Impact on Apps

## Test Goal
Verify that when we change `/libs/profile`, both apps that depend on it are rebuilt, but they are built **independently** (not as part of a single build).

## Test Setup

### Dependency Graph
```
account-app
├── greeter (unique to account-app)
└── profile (shared)

inventory-app
└── profile (shared)
```

Both apps depend on `profile`, so changes to `profile` should trigger rebuilds in both apps **when they are built**.

## Test Execution

### Step 1: Baseline - Clean Build Both Apps
```bash
cd apps/account-app
../../gradlew clean build --no-daemon -q
# ✅ account-app built

cd ../inventory-app
../../gradlew clean build --no-daemon -q
# ✅ inventory-app built
```

### Step 2: Modify Profile.java
Changed:
```java
public String getCurrentProfile() {
    return "Alice";
}
```

To:
```java
public String getCurrentProfile() {
    return "Alice - PROFILE MODIFIED";
}
```

### Step 3: Build account-app (INDEPENDENTLY)
```bash
cd apps/account-app
../../gradlew build --no-daemon
```

**Result:**
```
BUILD SUCCESSFUL in 4s
15 actionable tasks: 10 executed, 5 up-to-date
```

**Verification - Run account-app:**
```bash
../../gradlew run --console=plain --no-daemon
```

**Output:**
```
> Task :run
[account-service]: Hi, Alice - PROFILE MODIFIED. Hello world from Greeter.
```

✅ **Confirmed**: account-app automatically rebuilt profile and incorporated the change.

### Step 4: Build inventory-app (INDEPENDENTLY)
```bash
cd apps/inventory-app
../../gradlew build --no-daemon
```

**Result:**
```
BUILD SUCCESSFUL in 3s
13 actionable tasks: 8 executed, 5 up-to-date
```

**Verification - Run inventory-app:**
```bash
../../gradlew run --console=plain --no-daemon
```

**Output:**
```
> Task :run
[inventory-app] Hi, Alice - PROFILE MODIFIED.
```

✅ **Confirmed**: inventory-app also automatically rebuilt profile and incorporated the change.

## Key Findings

### ✅ Finding 1: Both Apps Rebuild When Profile Changes
When `libs/profile` is modified:
- ✅ `account-app` automatically rebuilds `profile` when you build `account-app`
- ✅ `inventory-app` automatically rebuilds `profile` when you build `inventory-app`

### ✅ Finding 2: Apps Are Built INDEPENDENTLY
- Each app has its own Gradle build process
- Each app declares `includeBuild("../../libs/profile")` in its own `settings.gradle.kts`
- Building `account-app` does NOT build `inventory-app`
- Building `inventory-app` does NOT build `account-app`
- Each app's Gradle daemon manages its own composite build

### ⚠️ Important Clarification

**Your question**: "verify that if we change something in /libs/profile only inventory-app is build and not account-app"

**Reality**: This statement is **misleading** because it depends on WHEN you build:

#### Scenario A: Only Build inventory-app
```bash
# Change libs/profile
cd apps/inventory-app
../../gradlew build
```
**Result**: ✅ Only `inventory-app` (and its dependencies) are built. `account-app` is NOT built.

#### Scenario B: Only Build account-app
```bash
# Change libs/profile
cd apps/account-app
../../gradlew build
```
**Result**: ✅ Only `account-app` (and its dependencies) are built. `inventory-app` is NOT built.

#### Scenario C: Build Both Apps Separately
```bash
# Change libs/profile
cd apps/account-app
../../gradlew build  # Builds account-app + profile

cd ../inventory-app
../../gradlew build  # Builds inventory-app + profile (independently)
```
**Result**: ✅ Both apps are built, but as **separate, independent builds**.

## What Does NOT Happen

❌ **Composite builds do NOT create a "global build"** where changing one library automatically builds all consuming apps in one go.

❌ **There is NO automatic cross-app rebuild**. Each app must be built explicitly.

❌ **Building inventory-app does NOT prevent account-app from needing to rebuild** if profile changed.

## What DOES Happen

✅ **Each app independently manages its composite builds**. When you build an app:
1. Gradle checks if any `includeBuild` dependencies changed
2. Rebuilds those dependencies automatically
3. Uses the fresh artifacts

✅ **Isolation**: Each app build is isolated. Building one app doesn't affect the other.

✅ **Incremental builds per app**: Each app tracks its own build state.

## Comparison with Your Expectation

### You Expected:
> "Change `/libs/profile` → ONLY inventory-app rebuilds, account-app does NOT rebuild"

### Reality:
- Change `/libs/profile`
- Build `inventory-app` → inventory-app rebuilds (with profile) ✅
- Build `account-app` → account-app ALSO rebuilds (with profile) ✅

**Both apps will rebuild when built** because they both depend on profile. The key is:
- They rebuild **independently** when you build them
- One doesn't force the other to build
- But each will detect the profile change when you build it

## If You Want Different Behavior

If you want "change profile → ONLY inventory-app builds, NOT account-app", you would need:

1. **CI/CD pipeline** that detects which files changed and selectively builds affected apps
2. **Build orchestration tool** (like Bazel, Nx, Turborepo) that tracks global dependencies
3. **Custom Gradle task** that analyzes changes and builds only affected projects

**Gradle Composite Builds alone do NOT provide this**. They provide:
- Automatic dependency resolution from source
- Independent app builds
- Incremental builds per app

But NOT:
- Global change detection across all apps
- Automatic selective building of only affected apps

## Conclusion

### Question: "If we change `/libs/profile`, is ONLY inventory-app built?"

**Answer**: **It depends on what you build**.

- If you only run `../../gradlew build` in `inventory-app` → Yes, only inventory-app is built ✅
- If you only run `../../gradlew build` in `account-app` → No, account-app is also built ✅
- Composite builds don't automatically decide which apps to build based on what changed

### Verified Behavior:

1. ✅ Changing `profile` triggers rebuild in ANY app that includes it via `includeBuild()`
2. ✅ Apps are built independently (building one doesn't build the other)
3. ✅ Each app automatically rebuilds its `includeBuild` dependencies when needed
4. ❌ Gradle does NOT prevent account-app from building if you explicitly build it after changing profile

The independence of composite builds means:
- **Good**: Each app can be built separately without coordination
- **Limitation**: You need external tooling (CI/CD) to intelligently decide which apps to build based on what changed
