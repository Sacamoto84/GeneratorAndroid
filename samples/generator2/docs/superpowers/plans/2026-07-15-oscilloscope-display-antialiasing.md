# Oscilloscope Display Antialiasing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the stationary display-aliasing pattern at oscilloscope sweeps `32` and higher while retaining the density-based glow and exact sample amplitude.

**Architecture:** Keep audio capture and the native direct buffer unchanged. The GLES renderer will explicitly enable SRC-alpha blending and give the vertex shader a per-buffer seed, surface width, and boolean dither flag. The vertex shader will apply a zero-mean horizontal offset no larger than half a physical pixel only at high sweeps; Y remains the original signal level.

**Tech Stack:** Kotlin, Android OpenGL ES 3.0, GLSurfaceView, JUnit 4, Gradle.

## Global Constraints

- Enable display dither only when `compressorCount >= 32f` and `width > 0`.
- Offset X by no more than `±0.5` physical pixel; never modify Y or the audio/native buffer.
- Change the dither seed only when `updateVerticesDirect()` receives a new buffer; a paused image must remain still.
- Use `GL_SRC_ALPHA` and `GL_ONE_MINUS_SRC_ALPHA` blending.
- Preserve the two-channel colors, channel visibility, combined/split layouts, and existing point-size behavior.

---

## File Structure

- Modify: `app/build.gradle` — make JUnit 4 available to local JVM unit tests.
- Modify: `app/src/main/java/com/example/generator2/features/scope/opengl/render/MyGLRendererOscill.kt` — own the blending state, dither policy invocation, uniforms, seed, and GLSL hash.
- Create: `app/src/test/java/com/example/generator2/features/scope/opengl/render/OscilloscopeDisplayDitherTest.kt` — cover the threshold and invalid-width policy without requiring an OpenGL device.

### Task 1: Implement and verify high-sweep display dither

**Files:**
- Create: `app/src/test/java/com/example/generator2/features/scope/opengl/render/OscilloscopeDisplayDitherTest.kt`
- Modify: `app/build.gradle` in `dependencies`
- Modify: `app/src/main/java/com/example/generator2/features/scope/opengl/render/MyGLRendererOscill.kt:3-39,53-126,159-179,205-235,274-281`

**Interfaces:**
- Consumes: `compressorCount: Float` and the current `width: Int` from `MyGLRendererOscill`.
- Produces: `internal fun isDisplayDitherEnabled(compressorCount: Float, viewportWidth: Int): Boolean` and GLSL uniforms `ditherEnabled`, `viewportWidth`, and `ditherSeed`.

- [ ] **Step 1: Add the test dependency and write a failing policy test.**

  In `app/build.gradle`, replace the commented JUnit declaration with:

  ```groovy
  testImplementation 'junit:junit:4.13.2'
  ```

  Create `app/src/test/java/com/example/generator2/features/scope/opengl/render/OscilloscopeDisplayDitherTest.kt` with:

  ```kotlin
  package com.example.generator2.features.scope.opengl.render

  import org.junit.Assert.assertFalse
  import org.junit.Assert.assertTrue
  import org.junit.Test

  class OscilloscopeDisplayDitherTest {

      @Test
      fun ditherIsEnabledOnlyForHighSweepsWithKnownSurfaceWidth() {
          assertFalse(isDisplayDitherEnabled(compressorCount = 16f, viewportWidth = 1080))
          assertFalse(isDisplayDitherEnabled(compressorCount = 31.999f, viewportWidth = 1080))
          assertFalse(isDisplayDitherEnabled(compressorCount = 32f, viewportWidth = 0))
          assertTrue(isDisplayDitherEnabled(compressorCount = 32f, viewportWidth = 1080))
          assertTrue(isDisplayDitherEnabled(compressorCount = 256f, viewportWidth = 1))
      }
  }
  ```

- [ ] **Step 2: Run the test and confirm it fails because the policy does not yet exist.**

  Run:

  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.example.generator2.features.scope.opengl.render.OscilloscopeDisplayDitherTest"
  ```

  Expected: the Kotlin test compilation fails with `Unresolved reference: isDisplayDitherEnabled`.

- [ ] **Step 3: Add the minimal, testable dither policy.**

  Immediately before `class MyGLRendererOscill`, add:

  ```kotlin
  internal fun isDisplayDitherEnabled(compressorCount: Float, viewportWidth: Int): Boolean {
      return compressorCount >= 32f && viewportWidth > 0
  }
  ```

- [ ] **Step 4: Add explicit blending and the dither renderer state.**

  Add these GLES imports to `MyGLRendererOscill.kt`:

  ```kotlin
  import android.opengl.GLES30.GL_BLEND
  import android.opengl.GLES30.GL_ONE_MINUS_SRC_ALPHA
  import android.opengl.GLES30.GL_SRC_ALPHA
  import android.opengl.GLES30.glBlendFunc
  import android.opengl.GLES30.glEnable
  import android.opengl.GLES30.glUniform1i
  ```

  Add the seed next to `compressorCount`:

  ```kotlin
  private var ditherSeed = 0
  ```

  In `onSurfaceCreated`, after `glClearColor(...)` and before shader compilation, set the blend state:

  ```kotlin
  glEnable(GL_BLEND)
  glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
  ```

- [ ] **Step 5: Extend the vertex shader with bounded horizontal dither.**

  Declare these uniforms after `uniform float compressorCount;`:

  ```glsl
  uniform int ditherEnabled;
  uniform float viewportWidth;
  uniform float ditherSeed;
  ```

  Add this hash helper before `void main()`:

  ```glsl
  float ditherHash(float value) {
      return fract(sin(value) * 43758.5453123);
  }
  ```

  Replace the existing X calculation with the following code. It converts the random range `[0, 1)` into `[-0.5, 0.5)` physical pixels and then into normalized device coordinates. It does not alter `y`.

  ```glsl
  float x = float(gl_VertexID) * 2.0 / len - 1.0;
  if (ditherEnabled != 0) {
      float offsetPixels = ditherHash(float(gl_VertexID) + ditherSeed * 17.0) - 0.5;
      x += offsetPixels * 2.0 / viewportWidth;
  }
  ```

- [ ] **Step 6: Bind the new uniforms and advance the seed only for a new direct buffer.**

  In `onDrawFrame`, after setting `compressorCount`, add:

  ```kotlin
  val ditherEnabledHandle = glGetUniformLocation(program, "ditherEnabled")
  glUniform1i(
      ditherEnabledHandle,
      if (isDisplayDitherEnabled(compressorCount, width)) 1 else 0
  )

  val viewportWidthHandle = glGetUniformLocation(program, "viewportWidth")
  glUniform1f(viewportWidthHandle, width.toFloat())

  val ditherSeedHandle = glGetUniformLocation(program, "ditherSeed")
  glUniform1f(ditherSeedHandle, ditherSeed.toFloat())
  ```

  In `updateVerticesDirect()`, after setting `vertexBuffer.position(0)`, add a bounded increment:

  ```kotlin
  ditherSeed = (ditherSeed + 1) % 8192
  ```

  Keep `width` at `0` until `onSurfaceChanged` supplies a real viewport:

  ```kotlin
  var width: Int = 0
  var height: Int = 0
  ```

- [ ] **Step 7: Run the automated checks.**

  Run the focused policy test:

  ```powershell
  .\gradlew.bat :app:testDebugUnitTest --tests "com.example.generator2.features.scope.opengl.render.OscilloscopeDisplayDitherTest"
  ```

  Expected: `BUILD SUCCESSFUL` and one passing test.

  Then compile the debug app to verify the Kotlin integration. The device check below validates runtime GLES shader compilation:

  ```powershell
  .\gradlew.bat :app:assembleDebug
  ```

  Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Perform device validation.**

  On an OpenGL ES 3.0-capable device, use a periodic signal that previously produced a stationary pattern and check:

  1. At sweep `16`, the trace has no dither and looks unchanged.
  2. At sweeps `32`, `64`, and `128`, the trace has no stationary holes or fixed sparse pattern; brightness follows the local point density.
  3. Pausing freezes the exact image, including the dither distribution.
  4. Both channels remain correctly colored and work in the combined and split layouts.

- [ ] **Step 9: Commit the feature and test.**

  ```powershell
  git add -- app/build.gradle app/src/main/java/com/example/generator2/features/scope/opengl/render/MyGLRendererOscill.kt app/src/test/java/com/example/generator2/features/scope/opengl/render/OscilloscopeDisplayDitherTest.kt
  git commit -m "feat: dither high-sweep oscilloscope display"
  ```

## Plan Self-Review

- Spec coverage: Task 1 enables SRC-alpha blending, sends width and seed to the shader, ditheres X only at `32` and higher, preserves Y and the native buffer, and includes automated plus device checks for low/high sweeps, pause, and channel layouts.
- Placeholder scan: no deferred work, unspecified test, or ambiguous error-handling step remains.
- Type consistency: the test, renderer, and uniform binding all use `isDisplayDitherEnabled(compressorCount: Float, viewportWidth: Int)`; the shader consumes `int ditherEnabled` and two `float` uniforms.
