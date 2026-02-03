# GitHub Actions Caching Setup Guide

## Overview

This document explains the caching configuration for GitHub Actions workflows in this project. Proper caching significantly speeds up CI/CD builds by reusing dependencies and build artifacts.

---

## Caching Layers Implemented

### 1. **Gradle Dependency Cache** (via `actions/setup-java`)
```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'corretto'
    cache: 'gradle'  # ← Caches ~/.gradle/caches and ~/.gradle/wrapper
```

**What it caches:**
- Downloaded dependencies (JARs from Maven Central)
- Gradle wrapper distributions
- Plugin dependencies

**Cache key based on:**
- `**/gradle/wrapper/gradle-wrapper.properties`
- `**/*.gradle*`
- `**/gradle.properties`

---

### 2. **Gradle Build Action Cache** (via `gradle/gradle-build-action`)
```yaml
- name: Setup Gradle
  uses: gradle/gradle-build-action@v3
  with:
    cache-read-only: ${{ github.ref != 'refs/heads/main' }}
    gradle-home-cache-cleanup: true
```

**What it caches:**
- Gradle build cache (compiled classes, test results)
- Configuration cache
- Gradle daemon state
- Build scan data

**Cache strategy:**
- **Main branch**: Read-write cache (can update cache)
- **Feature branches**: Read-only cache (can read but not update)
- This prevents cache pollution from temporary branches

---

### 3. **Gradle Build Cache** (via `--build-cache` flag)
```yaml
- name: Build account-app
  run: cd apps/account-app && ../../gradlew build --no-daemon --build-cache
```

**What it does:**
- Reuses task outputs across builds
- Skips tasks if inputs haven't changed
- Works across different machines (remote build cache)

---

## How Caching Works

### First Run (Cache Miss)
```
1. Download Gradle 8.5 distribution          (~110 MB, ~30s)
2. Download dependencies                     (~50 MB, ~15s)
3. Compile source code                       (~5s)
4. Run tests                                 (~3s)
5. Create artifacts                          (~2s)
6. Save cache                                (~10s)
---
Total: ~65 seconds
```

### Subsequent Runs (Cache Hit)
```
1. Restore Gradle cache                      (~2s)
2. Restore dependency cache                  (~3s)
3. Compile source code (if changed)          (~5s)
4. Run tests (if changed)                    (~3s)
5. Create artifacts                          (~2s)
---
Total: ~15 seconds (4x faster!)
```

---

## Cache Configuration Details

### Cache Read-Only for Feature Branches

```yaml
cache-read-only: ${{ github.ref != 'refs/heads/main' }}
```

**Why?**
- Feature branches can read from main's cache
- Feature branches cannot pollute the cache
- Only successful main branch builds update the cache
- Prevents cache thrashing from experimental changes

**Behavior:**
- `main` branch: `cache-read-only: false` (read-write)
- Feature branches: `cache-read-only: true` (read-only)

---

### Gradle Home Cache Cleanup

```yaml
gradle-home-cache-cleanup: true
```

**What it does:**
- Removes unused files before saving cache
- Reduces cache size
- Prevents cache bloat over time
- Keeps only necessary files

---

### Build Cache Flag

```yaml
--build-cache
```

**Benefits:**
- Task output caching across builds
- Incremental builds even on fresh clones
- Shared cache across CI runs
- Faster builds for unchanged code

---

## Expected Performance Improvements

### Before Caching
```
account-app build:
- Gradle download: 30s
- Dependency resolution: 15s
- Compilation: 5s
- Tests: 3s
- Packaging: 2s
Total: ~55 seconds
```

### After Caching (Cache Hit)
```
account-app build:
- Cache restore: 5s
- Compilation (unchanged): 0s (UP-TO-DATE)
- Tests (unchanged): 0s (UP-TO-DATE)
- Packaging: 2s
Total: ~7 seconds (7.8x faster!)
```

### After Caching (Partial Changes)
```
account-app build (only AccountApp.java changed):
- Cache restore: 5s
- Compilation: 2s (only changed classes)
- Tests: 3s
- Packaging: 2s
Total: ~12 seconds (4.5x faster!)
```

---

## Cache Locations

GitHub Actions caches are stored in:
- **Location**: GitHub's cache storage (per repository)
- **Size limit**: 10 GB total per repository
- **Retention**: 7 days for unused caches
- **Scope**: Available to all workflows in the repository

### What Gets Cached

```
~/.gradle/
├── caches/                    # Dependency JARs, artifacts
│   ├── modules-2/
│   ├── transforms-3/
│   └── ...
├── wrapper/                   # Gradle wrapper distributions
│   └── dists/
├── build-cache/              # Build cache (task outputs)
└── configuration-cache/      # Configuration cache
```

---

## Monitoring Cache Performance

### Check Cache Usage
1. Go to: `https://github.com/mehrajmalik-full/gradle-multi-project-monorepo/actions/caches`
2. View cache entries, sizes, and hit rates

### Check Build Times
Compare workflow run times:
- First run after cache clear: ~55 seconds
- Subsequent runs with cache: ~7-15 seconds

### Enable Build Scans
The workflows include `--scan` which provides:
- Detailed task execution times
- Cache hit rates
- Dependency resolution times
- Performance insights

Access build scans at: https://scans.gradle.com

---

## Cache Invalidation

Caches are automatically invalidated when:
- Any `*.gradle.kts` file changes
- `gradle.properties` changes
- `gradle-wrapper.properties` changes
- 7 days pass without use

Manual cache invalidation:
1. Go to repository Actions → Caches
2. Delete specific cache entries
3. Next build will recreate cache

---

## Best Practices Implemented

### ✅ 1. Multi-Layer Caching
- Java dependency cache (`actions/setup-java`)
- Gradle build cache (`gradle/gradle-build-action`)
- Task output cache (`--build-cache`)

### ✅ 2. Smart Cache Strategy
- Read-write for main branch
- Read-only for feature branches
- Prevents cache pollution

### ✅ 3. Cache Cleanup
- Automatic cleanup before save
- Prevents bloat
- Keeps cache size manageable

### ✅ 4. Incremental Builds
- `--build-cache` flag
- Only rebuilds what changed
- Shares outputs across runs

### ✅ 5. Path Triggers
- Added `gradle/wrapper/**` to trigger paths
- Ensures cache refresh when Gradle updates

---

## Troubleshooting

### Cache Not Being Used

**Problem**: Build times haven't improved

**Solutions:**
1. Check cache hit rate in build logs:
   ```
   Look for: "Restored ... from cache"
   ```

2. Verify cache exists:
   - Go to Actions → Caches
   - Check for recent cache entries

3. Ensure cache key matches:
   - Check if `gradle-wrapper.properties` changed
   - Check if any `*.gradle.kts` files changed

### Cache Size Too Large

**Problem**: Cache approaching 10 GB limit

**Solutions:**
1. Enable cleanup (already done):
   ```yaml
   gradle-home-cache-cleanup: true
   ```

2. Review dependencies:
   - Remove unused dependencies
   - Exclude transitive dependencies

3. Clear old caches:
   - Delete unused cache entries manually

### Slow First Build

**Problem**: First build after cache clear is slow

**Expected**: This is normal
- Gradle needs to download distribution
- Dependencies need to be resolved
- First compilation has no cache

**Solution**: Wait for cache to populate on first run

---

## Advanced Configuration (Optional)

### Custom Cache Key

If you need more control over cache invalidation:

```yaml
- name: Cache Gradle packages
  uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    restore-keys: |
      ${{ runner.os }}-gradle-
```

### Remote Build Cache

For even better performance across teams:

1. Set up a remote build cache server (e.g., Gradle Enterprise)
2. Configure in `gradle.properties`:
   ```properties
   org.gradle.caching=true
   org.gradle.caching.remote.enabled=true
   org.gradle.caching.remote.url=https://your-cache-server.com
   ```

---

## Verification

### Test the Cache Setup

1. **Trigger a fresh build:**
   ```bash
   # Make a small change
   echo "// cache test" >> apps/account-app/src/main/java/account/AccountApp.java
   git add .
   git commit -m "Test cache setup"
   git push origin main
   ```

2. **Check first run:**
   - Go to Actions tab
   - Note the build time (~55 seconds)
   - Look for cache save messages

3. **Trigger another build:**
   ```bash
   # Make another small change
   echo "// cache test 2" >> apps/account-app/src/main/java/account/AccountApp.java
   git add .
   git commit -m "Test cache hit"
   git push origin main
   ```

4. **Check second run:**
   - Should be much faster (~15 seconds)
   - Look for "Restored from cache" messages

---

## Summary

### Current Configuration

✅ **Multi-layer caching implemented**
- Java dependency cache via `actions/setup-java`
- Gradle build cache via `gradle/gradle-build-action`
- Task output cache via `--build-cache`

✅ **Smart cache strategy**
- Read-write for main branch
- Read-only for feature branches

✅ **Cache cleanup enabled**
- Automatic cleanup before save
- Prevents cache bloat

✅ **Build scans enabled**
- Performance insights
- Cache hit rate tracking

### Expected Results

- **First build**: ~55 seconds (cache population)
- **Subsequent builds**: ~7-15 seconds (cache hit)
- **Performance improvement**: 4-8x faster
- **Cache size**: ~200-500 MB per project
- **Cache retention**: 7 days

---

## Additional Resources

- [Gradle Build Action Documentation](https://github.com/gradle/gradle-build-action)
- [GitHub Actions Caching](https://docs.github.com/en/actions/using-workflows/caching-dependencies-to-speed-up-workflows)
- [Gradle Build Cache](https://docs.gradle.org/current/userguide/build_cache.html)
- [Build Scans](https://scans.gradle.com)
