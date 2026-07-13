# Build Issue Resolved: SDK and Build Tools Upgrade

The project build was failing due to library requirements (`androidx.hilt:hilt-navigation-compose:1.4.0`) that needed Android API 37 and Android Gradle Plugin (AGP) 9.1.0 or higher.

## Changes Made

### 1. SDK Upgrade
- Updated `compileSdk` and `targetSdk` to **37** in `:app/build.gradle`.

### 2. Build Tools Upgrade
- Upgraded **Android Gradle Plugin** to `9.1.1` in the root `build.gradle`.
- Upgraded **Gradle Wrapper** to `9.6.1` in `gradle-wrapper.properties`.
- Upgraded **Hilt Plugin** to `2.60.1` to match the library versions used in the project.

### 3. Compatibility Fixes
- Added `android.builtInKotlin=false` and `android.newDsl=false` to `gradle.properties`. This allows the project to continue using the `org.jetbrains.kotlin.android` plugin with AGP 9.x while avoiding "duplicate extension" errors caused by AGP's new built-in Kotlin support.
- Fixed implicit dependency issues with the **libres** plugin by explicitly declaring task dependencies in `:app/build.gradle`.

### 4. Build Script Cleanup
- Removed redundant `classpath` declarations from the root `buildscript` block, moving all plugins to the modern `plugins` block.

## Verification Results

- **Gradle Sync**: Successful.
- **Build (`:app:assembleDebug`)**: Successful.

> [!TIP]
> AGP 9.0+ introduces built-in Kotlin support. While we bypassed it for now to maintain project stability, you might consider migrating to it in the future.
