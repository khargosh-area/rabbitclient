# REA investigation — follow-up 2: Android APK class and storage map

Date: 2026-10-20

## Method

Performed offline ZIP inventory and printable-string/class-descriptor extraction on the user-supplied `Flarial (1).apk`. This is not JADX decompilation. Class names and strings are useful leads, but do not by themselves prove a method's control flow or runtime behavior.

## Package and artifact facts

- Package: `xyz.flarial.client`
- Reported version: `1.3.20.5 (2026091201)`
- DEX files: `classes.dex`, `classes2.dex`, `classes3.dex`, `classes4.dex`
- APK archive contained no `.so` entries. Strings include paths/names for `libminecraftpe.so`; that is not proof the library is packaged in this APK.
- Android entrypoint and major app class names include `MainActivity`, `LauncherApplication`, `MinecraftActivity`, `MinecraftPreloadService`, and `BackgroundConnectionService`.

## Class-name groups visible in DEX descriptors

The following names appear in the DEX descriptor data:

- **Launcher/game handoff:** `com.flarial.client.Launcher.MinecraftActivity`, `MinecraftPreloadService`, `BackgroundConnectionService`, `LauncherSearchHost`.
- **Main app and data manager:** `MainActivity`, `MCDataManagerActivity`, `MinecraftDataDocumentProvider`, `FlarialDocumentProvider`, `FlarialCrashLogsDocumentProvider`.
- **Minecraft content models:** `MinecraftResourcePack`, `MinecraftBehaviorPack`, `MinecraftSkinPack`, `MinecraftStorageLocation`.
- **Backup pipeline:** `BackupActivity`, `BackupTarget`, `backup.FlBackupInspection`, `FlBackupRequest`, `FlBackupProgress`, `FlBackupRestorePreflight`, `FlBackupRestoreProgress`, and `FlBackupConflictPolicy`.
- **Migration and recovery:** `datamigration.MigrationActivity`, `MigrationOptionsActivity`, `MigrationControlProvider`, `MigrationRecoveryProvider`, `MigrationSnapshotsDocumentProvider`, `MigrationStreamProvider`, and `MigrationTransferService`.
- **Account flow:** `auth.MsftLoginActivity`.

Some migration implementation classes are obfuscated (short names), so their roles should not be guessed from names alone.

## Storage/access evidence from strings

Strings mention the Minecraft package IDs `com.mojang.minecraftpe`, `com.mojang.minecraftpe.beta`, and `com.mojang.minecraftpe.preview`, and storage paths under `games/com.mojang`, including `minecraftWorlds` and pack directories. Other messages describe a Shizuku permission flow for selected worlds and packs. Treat this as evidence of supported access paths in the app, not as a universal permission guarantee across Android versions or devices.

The APK also contains user-facing backup validation messages about ZIP entry paths, duplicate entries, expansion/size limits, checksums and staging. This suggests the backup implementation intentionally defends against malformed archives. Rabbit Client should apply equivalent defensive validation if it implements archive import/export.

## What can be concluded

The APK has a distinct launcher/game activity and service layer, a dedicated Minecraft data manager, and explicit backup/migration components. Those are strong architecture clues for planning Rabbit Client's Android app. They do not reveal a complete in-game module bridge.

## What remains unverified

- Exact Android manifest component permissions and exported status have not been decoded because the binary manifest has not been processed with a manifest decoder.
- The Java/Kotlin method bodies have not been decompiled or traced.
- The native bridge and actual runtime loading path remain unknown.
- No app/game execution was performed and no device logs were captured.
- No REA MCP session was run in this environment; this report is offline artifact triage.

## Recommended next step

Run REA's documented Android inspection commands on the exact APK in a supported local setup, then trace `MinecraftActivity`, `MinecraftPreloadService`, `MCDataManagerActivity`, and the backup request/preflight/restore classes. Capture evidence for each call path before implementing a compatible Rabbit Client counterpart. Official reference: https://github.com/morluto/rea
