# Update Android SDK and Build Tools

The project build is failing because `androidx.hilt:hilt-navigation-compose:1.4.0` and other libraries require:
1. `compileSdk 37` or later.
2. Android Gradle Plugin (AGP) `9.1.0` or higher.

The current project uses `compileSdk 36`, AGP `8.13.2`, and Gradle `8.13`.

## Proposed Changes

### [app](file:///G:/GeneratorAndroid/samples/generator2/app/build.gradle)
#### [MODIFY] [build.gradle](file:///G:/GeneratorAndroid/samples/generator2/app/build.gradle)
- Update `compileSdk` to 37.
- Update `targetSdk` to 37.

### [Root Project](file:///G:/GeneratorAndroid/samples/generator2/build.gradle)
#### [MODIFY] [build.gradle](file:///G:/GeneratorAndroid/samples/generator2/build.gradle)
- Update AGP versions (`com.android.application`, `com.android.library`, `com.android.test`) to `9.2.1`.
- Update `androidx.baselineprofile` to a compatible version if needed (checking `1.3.3`).

### [Gradle Wrapper](file:///G:/GeneratorAndroid/samples/generator2/gradle/wrapper/gradle-wrapper.properties)
#### [MODIFY] [gradle-wrapper.properties](file:///G:/GeneratorAndroid/samples/generator2/gradle/wrapper/gradle-wrapper.properties)
- Update `distributionUrl` to use Gradle `9.6.1`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify that the project builds successfully.
- Address any remaining task dependency issues (e.g., `libres`).
