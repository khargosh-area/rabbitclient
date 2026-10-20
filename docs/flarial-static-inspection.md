# Flarial APK: preliminary static inspection

Date: 2026-10-20

## Scope and evidence

This is a preliminary, offline inspection of the supplied Flarial APK, not a full REA investigation and not a claim that original source code has been recovered.

- APK filename supplied in the analysis workspace: `Flarial (1).apk`
- SHA-256: `c32f93ff57d04ff1c1f07ac1fbcb3ce18ae0b477bfbac87c4a75f8a349523c7c`
- Reported package/version metadata from the earlier APK inspection: `xyz.flarial.client`, version `1.3.20.5 (2026091201)`
- Archive inventory: 1,073 entries; four DEX files (`classes.dex`, `classes2.dex`, `classes3.dex`, `classes4.dex`); no `.so` files in the APK archive.
- The archive includes `assets/licenses/LeviLaunchroid-LICENSE.txt`, `assets/licenses/LeviLaunchroid-NOTICE.txt`, `assets/licenses/Space-Grotesk-OFL.txt`, and `assets/licenses/fidelityfx-fsr.txt`. Preserve applicable notices if corresponding code/assets are reused.
- Strings also reference `lib/armeabi-v7a/libminecraftpe.so` as a runtime target/path, but the APK archive inspected here does not contain that native library.

## High-confidence observations from embedded strings

1. **Launcher-to-game lifecycle:** strings identify `com.flarial.client.Launcher.MinecraftActivity`, Minecraft startup/return handling, and a marker for whether the Minecraft activity is running. This supports a distinct launcher/game integration boundary.
2. **Minecraft file access:** strings refer to worlds under `games/com.mojang/minecraftWorlds`, resource packs, behavior packs, skin packs, and options.
3. **Shizuku-based access:** multiple messages explicitly describe requesting Shizuku permission and using a remote command backend for Minecraft external-storage operations. This is an important optional integration and permission boundary, not a generic filesystem permission.
4. **Backup/restore pipeline:** the APK contains a `.flbackup` format and numerous checks for invalid ZIP paths, duplicate entries, excessive expansion, checksums, declared sizes, and staging directories. Any Rabbit Client backup implementation should retain strict ZIP traversal, duplicate-entry, size, checksum, and staging validation.
5. **Config/profile and runtime state:** strings include Flarial config/migration markers, instance-related identifiers, Minecraft staging state, and native-crash reporting markers. These suggest that configuration, launch state, and backup/migration state should be modeled separately.
6. **Third-party components:** strings reveal Android/Jetpack, ads/analytics, Firebase, and Shizuku dependencies. Their presence in an APK is not evidence that Rabbit Client needs or should copy those SDKs.

## What this inspection does not prove

- It does not identify the complete native injection/client-modification mechanism.
- It does not establish that any individual gameplay module is functional, or reconstruct its logic.
- It does not include decompiled Kotlin/Java source, a native disassembly, call graphs, or runtime traces.
- It does not prove that the observed strings correspond to reachable code paths.
- No REA MCP/native-analysis session was run in this environment. The available Node.js is `v22.16.0`, below the current REA README's stated `22.19+` minimum, and the environment has no `rea`, `jadx`, or `apktool` executable available. Therefore this document must not be described as a full REA result.

## Rabbit Client design implications

- Keep launcher UI, instance management, storage access, game launch, and in-game client integration as separate modules.
- Implement file access through explicit, user-approved Android document/provider or Shizuku flows where appropriate; explain permissions before requesting them.
- Treat backups as untrusted input and validate them before extraction or restore.
- Do not infer a working in-game module from a launcher toggle. Require a verified game integration path and a repeatable test for each module.
- Keep only required third-party notices and licenses, and review their terms before adapting code.

## Next investigation steps

1. Run the official REA setup on a supported local machine (Node.js 22.19+, 24.11+, or 26+) and configure an appropriate analysis provider.
2. Decompile the DEX files with a supported Android decompiler, then trace the launcher activity, Minecraft launch handoff, module/config registry, and storage/backup code.
3. Inspect the actual Minecraft integration/native bridge separately. The supplied APK has no native `.so` payload, so runtime extraction/cache locations and supported device behavior need to be examined rather than guessed.
4. Record evidence per feature: class/method or native symbol, call path, observed behavior, test environment, and remaining uncertainty.
