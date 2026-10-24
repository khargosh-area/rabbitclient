# REA, LeviLauncher, and Flarial integration notes

Date: 2026-10-24

## LeviLauncher UI research

Reference source: https://github.com/LiteLDev/LeviLauncher and user guide: https://docs.levimc.org/LeviLauncher/.

LeviLauncher is a Windows Minecraft Bedrock (GDK/UWP) desktop launcher, not an Android launcher. Its user-facing workflow emphasizes a version-first instance list, separate installation/profile management, mod/content tools, and appearance settings (wallpaper, tint, opacity, and readability controls). Rabbit Client should translate those workflow ideas into its own Compose navigation and visual language rather than copying desktop components or assets wholesale.

The upstream repository is GPL-3.0-only. Review the license and third-party notices before directly reusing code. This implementation uses the high-level workflow ideas; it does not copy LeviLauncher source files.

## REA state and evidence limits

Official REA project: https://github.com/morluto/rea.

The current workspace did not have a working REA toolchain or JADX installation when the previous static-inspection notes were prepared. The existing notes under docs/rea-investigation-followup-*.md describe offline APK/source-tree inspection, not a completed REA MCP session. Do not label those findings as dynamic execution traces or a complete APK decompilation.

For a full investigation, use a supported REA setup, run its documented Android package and class/method inspection commands against an authorized local APK, and record the exact APK digest and method evidence before deciding how to implement a runtime bridge.

## Flarial module portability

The supplied Flarial source notes identify a Windows-native C++23/MSVC target with DirectX-specific render hooks and Windows-oriented dependencies. The Android launcher is Kotlin/Jetpack Compose. Flarial's Windows module source is useful for studying concepts such as module metadata, typed settings, enable/disable lifecycle, and normalized HUD coordinates, but it cannot be compiled directly into the Android app.

A launcher toggle is not a gameplay module. A real feature needs a verified Minecraft-side runtime/bridge, capability checks, cleanup on disable, settings persistence, and device/game-version tests. Until that bridge exists, the app must not display Flarial features as working in-game controls. Review the source archive's AGPL-3.0 terms and third-party licenses before adapting any implementation.

## Installations and packs

Rabbit Client's Android instance list intentionally reports only game packages it can identify and launch through Android's normal package APIs. This code does not download, patch, or bypass licensing for Minecraft game packages.

The CurseForge implementation expects the official API through the separately deployed backend/ service. The API key remains server-side. The app downloads supported pack files and hands them to Android/Minecraft through a content URI; it does not silently write into Minecraft's private directory.
