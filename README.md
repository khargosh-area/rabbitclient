# Rabbit Client

Rabbit Client is an Android launcher project for Minecraft Bedrock with an original, performance-minded UI and a separate client-integration layer.

## Authors

**KHARGOSH & CHATGPT**

Rabbit Client's interface is an original Android design inspired by common launcher patterns: game/version header, launch action, instance selection, and content discovery. It is not the LeviLauncher application or a claim of identical functionality. Flarial's desktop launcher is used as a workflow reference only; its Windows/Linux game runtime is not an Android runtime. Third-party code and assets must retain their applicable licenses and notices.

## Current status

**Version:** 0.1.0 starter  
**Target game range:** Minecraft Bedrock/MCPE 1.21.90 and newer, including future versions only where compatibility is verified.  
**Current reality:** the launcher detects installed Android Minecraft editions (Stable, Preview, Beta where present), lets the user select one, saves that selection, and launches the selected installed package. It does not download arbitrary Minecraft versions or create isolated copies of the game. In-game modules are intentionally absent from the launcher UI until a verified Android game integration exists.

## Visual direction

- Premium Obsidian + Mint dark theme
- Rounded panels, restrained borders, clear typography, readable contrast
- Performance-first UX: avoid unnecessary animation and heavy backgrounds
- Keep the launcher focused on launch, instances, content discovery and settings

## Planned areas

- Launcher dashboard and game-version detection
- Instance management and supported launch flow
- Safe backup/import/export workflows
- Worlds, packs, and add-on discovery
- Theme, accent, background and accessibility settings
- Managed isolated game runtime and independent version installs, if a legitimate Android-compatible launch path can be implemented
- In-game module registry and movable/scalable HUD only after a verified Android-compatible game integration exists

## Version support policy

The intended minimum is 1.21.90. Minecraft began using year-based version numbers in 2026, so future versions may be 26.x rather than 1.22.x. Do not hardcode a finite version list. Detect the installed version when possible, and distinguish launcher compatibility from verified in-game integration. See [the version support policy](docs/minecraft-version-support.md).

## Build

Open this project in Android Studio with a compatible Android SDK and JDK 17. GitHub Actions builds a debug APK and publishes it as a workflow artifact. A successful CI build does not replace testing on the target Android device.

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

Keep launcher UI, instance selection, storage and launch adapter separated. Add a managed runtime only with a legal, supported Android launch path. Add in-game modules only after the game integration is verified. Mark unsupported capabilities honestly and add reproducible tests before claiming a feature works.
