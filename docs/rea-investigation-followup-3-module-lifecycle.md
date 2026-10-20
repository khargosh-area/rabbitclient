# REA investigation — follow-up 3: module lifecycle and HUD design notes

Date: 2026-10-20

## Source files inspected

This note is based on reading the supplied archive's `Module.cpp`, `Module.hpp`, `HUDModule.hpp`, `HUDModule.cpp`, sample module source files, `ClickGUI.cpp`, and `Hook/Manager.cpp`. These are source-level observations only; the Windows target was not built or run.

## Module lifecycle findings

- The common module type holds metadata (name, tooltip/description, icon, aliases, keybind), settings, enabled/active state, and helper methods for setting controls.
- The source calls a post-construction initialization method that loads settings. Module configuration is not just a UI toggle; it is tied to module state and saved settings.
- The HUD base class registers its render-event listener on enable and deregisters it on disable, with explicit extension points for module-specific setup and cleanup.
- HUD display text is produced through a module-specific value method and passed into shared rendering logic. This is a useful separation between data retrieval and drawing.
- The shared HUD rendering code includes position percentages, scale, padding, text size, rectangle sizing, opacity, rounding, shadow and color-related settings. It also writes normalized X/Y coordinates based on window size, supporting layout persistence across different window dimensions.
- The hook manager explicitly registers game/render/visual hooks, including a gamma hook. This is a Windows game-side runtime mechanism and cannot be assumed to exist in the Android launcher.

## Design lessons for Rabbit Client

1. Use a stable module ID (not only a display name), category, description, required capabilities, and typed setting definitions.
2. Separate module state from the UI control. UI state should be persisted but must not imply gameplay effect without an active integration capability.
3. Define a lifecycle with predictable enable/disable cleanup. On disable, remove listeners and restore any temporary values changed by a module.
4. Store HUD X/Y as normalized coordinates (0.0–1.0), plus scale, alignment and optional size constraints. Clamp values and provide reset-to-default.
5. Separate the HUD model/layout editor from the renderer. The renderer should be supplied by a real game integration layer, not the Compose launcher.
6. Keep launcher preferences, module settings and per-profile HUD layout in separate namespaces/files so migrating one does not corrupt another.
7. Expose capability status per module (available, unsupported, integration missing, error) and prevent unsupported controls from masquerading as functional.
8. Exclude Reach-related functionality from all Rabbit Client surfaces and serialized defaults.

## Feature-specific caveats

- The presence of `Fullbright.cpp`, `FPS.cpp`, `Coordinates.cpp`, and other module source files establishes that the archive contains implementations or implementation units with those names. It does not prove they compile in this snapshot or work on current Minecraft builds.
- The source is Windows/DirectX oriented. Porting a conceptual design is different from copying its code. Review file-specific license and third-party terms before reusing any implementation.
- This analysis does not include offsets, hook signatures, game memory writes, or an injection recipe. Those details are not needed to build the independent launcher architecture and would be especially brittle across game versions.

## Verification still needed

- Build the Windows source with its documented MSVC/CMake toolchain if validating that target is necessary.
- Run REA/JADX on the Android APK to recover actual Kotlin/Java method bodies.
- Trace the Android game-launch flow and identify a supported integration bridge, if one exists.
- On a test device, validate launcher-only features first, then test each supported in-game capability separately.
