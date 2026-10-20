# REA investigation — follow-up 1: supplied source archive

Date: 2026-10-20

## New evidence inspected

A second supplied artifact is present in the workspace: `VYRIX_CLIENT_Flarial_Source_Mod.zip`. Its root folder is `dll-oss-main/`. This archive is a Windows-native Flarial source tree, not an Android launcher project.

The source README explicitly lists Windows 10/11 x64, MSVC, CMake and Minecraft Bedrock for Windows as its supported build context. Its CMake configuration declares C++23 and builds a shared library named `Flarial`. Therefore it cannot be dropped into Rabbit Client's Android Gradle/Compose project as-is.

## Architecture identified in the source tree

The following observations come from file paths and inspected headers/configuration; they are architecture-level findings, not a claim that the archive was compiled or executed.

- **Module base and settings:** `src/Client/Module/Modules/Module.hpp` describes a common module abstraction with display metadata, aliases, settings, keybind actions and enabled/active state.
- **HUD specialization:** `HUDModule.hpp` describes a base class for text HUDs. It standardizes default position/scale/color/alignment settings and delegates each displayed value to a module-specific implementation.
- **Events and hooks:** `src/Client/Events/` and `src/Client/Hook/` separate event types from hook implementations. The tree includes game, input, render and visual hooks, including DirectX-specific render paths.
- **ClickGUI:** `src/Client/Module/Modules/ClickGUI/` contains the in-game menu and UI elements. It is part of the native game-side library, not the Android launcher UI.
- **Scripting:** `src/Scripting/` and `lib/lua/lua-5.4.7/` indicate a Lua-based scripting subsystem, including script settings and client/player/world utility libraries.
- **Build dependencies:** CMake references Windows/MSVC-specific compilation flags and dependencies such as MinHook, FreeType, Lua and Windows rendering APIs. This reinforces the platform boundary.
- **Example module families present in the tree:** FPS, coordinates, CPS, keystrokes, potion HUD, fullbright, sprint, zoom, FOV, and movable HUD elements. Their filenames alone do not prove each feature works in every build or Minecraft version.

## Important compatibility conclusion

The source tree is useful for understanding concepts (module lifecycle, setting schemas, HUD layout, event separation), but its Windows DLL injection/hooks and DirectX implementation are not an Android/MCPE integration solution. Rabbit Client must have its own Android-compatible integration boundary and must not pretend that Compose switches invoke these native features.

The previously inspected Android APK and this Windows source archive are different artifacts. Their relationship and exact shared implementation have not been established. In particular, the Windows source README states that key portions are not in the open-source repository, so the archive cannot be assumed to reproduce all behavior of the distributed APK.

## Licensing and reuse

The archive contains an AGPL-3.0 license. Treat it as a license obligation, not blanket permission to copy any asset or dependency. Before adapting any source code, identify the exact file's copyright/license and third-party notices, keep required notices, and assess AGPL obligations for the intended distribution. Other assets and bundled dependencies may have their own terms. This follow-up report deliberately records architectural observations rather than copying source files into Rabbit Client.

## Rabbit Client implementation plan derived from the evidence

1. Keep the Android launcher, Minecraft launch adapter, in-game runtime adapter, module registry, settings persistence and HUD layout engine as distinct components.
2. Make modules consume a small, explicit capability interface (for example, read player coordinates or render a HUD item) rather than directly importing Android UI code or Windows SDK types.
3. Model module lifecycle and cleanup explicitly so disabling a module releases listeners and restores temporary game settings.
4. Persist per-module settings and per-profile HUD position/scale/alignment separately from launcher preferences.
5. Only enable a module when the active runtime advertises the capability it needs; show unsupported status otherwise.
6. Exclude the previously disallowed Reach-related feature family from Rabbit Client's UI, registry, configuration and documentation.
7. Verify the Android launcher build and game integration independently; neither static source inspection nor a successful launcher build proves gameplay integration works.

## Remaining gaps

- No REA MCP session has run: the current environment lacks the REA CLI, and the available Node.js runtime was below the official README's stated minimum at inspection time.
- The APK has not been decompiled into full Java/Kotlin source in this environment because JADX is unavailable.
- No Android device/emulator runtime trace or native game-side bridge was captured.
- No compile/test was run for the supplied Windows source archive.
- No conclusion is made about online/server behavior or anti-cheat outcomes from source names alone.

## Official REA workflow reference

The official REA repository documents Android APK inspection through commands such as `inspect-android-package`, `search-android-classes`, `inspect-android-class`, `inspect-android-method`, and `trace-android-references`; these require the configured REA toolchain and a local APK path. See https://github.com/morluto/rea and its Android analysis guide before running those commands in a supported environment.
