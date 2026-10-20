# Rabbit Client

Rabbit Client is an Android launcher project for Minecraft Bedrock with an original, performance-minded UI and a separate client-integration layer.

## Authors

**KHARGOSH & CHATGPT**

Rabbit Client's current interface is an original implementation inspired by the general launcher workflow used by LeviLauncher: game/version header, launch action, module preview, and content-management shortcuts. It is not the LeviLauncher application or a claim of identical functionality. Third-party code and assets must retain their applicable licenses and notices.

## Current status

**Version:** 0.1.0 starter  
**Target game range:** Minecraft Bedrock/MCPE 1.21.90 and newer, including future versions only where compatibility is verified.  
**Current reality:** the launcher UI is a prototype. Module switches are demo state and do not modify Minecraft. No FPS gain or in-game module behavior is claimed yet.

## Visual direction

- Premium Obsidian + Mint dark theme
- Rounded panels, restrained borders, clear typography, readable contrast
- Performance-first UX: avoid unnecessary animation and heavy backgrounds
- FPS Counter gets priority in the planned in-game HUD module set

## Planned areas

- Launcher dashboard and game-version detection
- Instance management and supported launch flow
- Safe backup/import/export workflows
- Worlds, packs, and add-on discovery
- Theme, accent, background and accessibility settings
- Independent module registry, typed settings, profiles and lifecycle
- Movable/scalable HUD layout and FPS/coordinates/CPS/Armor/Potion HUD modules
- Verified, Android-compatible in-game integration

## Version support policy

The intended minimum is 1.21.90. Minecraft began using year-based version numbers in 2026, so future versions may be 26.x rather than 1.22.x. Do not hardcode a finite version list. Detect the installed version when possible, and distinguish launcher compatibility from verified in-game integration. See [the version support policy](docs/minecraft-version-support.md).

## Build

Open this project in Android Studio with a compatible Android SDK and JDK 17. GitHub Actions is configured to build a debug APK and publish it as a workflow artifact. A successful build has not yet been confirmed.

## Investigation notes

- [Preliminary Android APK inspection](docs/flarial-static-inspection.md)
- [Windows source archive findings](docs/rea-investigation-followup-1.md)
- [Android class/storage map](docs/rea-investigation-followup-2-android-map.md)
- [Module lifecycle and HUD notes](docs/rea-investigation-followup-3-module-lifecycle.md)
- [Feature compatibility matrix](docs/compatibility-matrix.md)
- [Isolated instance architecture and acceptance checklist](docs/instance-architecture.md)

## Safety, licensing and attribution

Respect Minecraft, platform, and third-party project terms. Preserve required notices when integrating third-party code. The supplied Windows source archive includes an AGPL-3.0 license; review obligations before reusing any code. Do not distribute copyrighted assets without permission.

## Contributing

Keep launcher UI, storage, launch adapter, module registry, HUD layout and in-game runtime integration separated. Mark unsupported capabilities honestly. Add reproducible tests before claiming a feature works.
