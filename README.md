# Rabbit Client

<p align="center">
  <img src="app/src/main/res/drawable/rabbit_logo.xml" width="88" alt="Rabbit Client logo" />
</p>

<h1 align="center">Rabbit Client</h1>
<p align="center"><strong>A native Android companion launcher for Minecraft Bedrock.</strong></p>
<p align="center">
  <a href="https://github.com/khargosh-area/rabbitclient/actions/workflows/android.yml"><img src="https://github.com/khargosh-area/rabbitclient/actions/workflows/android.yml/badge.svg" alt="Android Build"></a>
  <img src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/language-Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/status-Preview-orange" alt="Preview status">
</p>

Rabbit Client is an Android app built with **Kotlin, Jetpack Compose and Material 3**. It provides a focused place to select and open an already-installed Minecraft Bedrock edition, browse community content through an optional CurseForge service, import supported Bedrock files, and personalize the launcher.

> **Project status:** Preview / work in progress. The app is a companion launcher, not a replacement Minecraft game client. Features that require code running inside Minecraft are not presented as working switches.

## Contents

- [What works today](#what-works-today)
- [Screens and features](#screens-and-features)
- [Important Android limitations](#important-android-limitations)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Build the Android app](#build-the-android-app)
- [Optional CurseForge service](#optional-curseforge-service)
- [Version and compatibility policy](#version-and-compatibility-policy)
- [Roadmap](#roadmap)
- [Security and privacy](#security-and-privacy)
- [Credits and licensing](#credits-and-licensing)
- [Contributing](#contributing)

## What works today

| Area | Current implementation | Status |
| --- | --- | --- |
| Native Android UI | Kotlin + Jetpack Compose + Material 3 | Implemented |
| Minecraft edition detection | Detects installed Stable, Preview and Beta package variants when Android exposes them | Implemented |
| Edition selection | Select an installed edition and retain the launcher selection | Implemented |
| Launch | Opens the selected installed Minecraft package using Android's package launch intent | Implemented |
| Theme and accent | Obsidian-style dark interface with selectable accent presets | Implemented |
| Wallpaper | Choose a local image through Android's document picker; persist its URI permission | Implemented |
| Glass opacity | Adjust translucent panel opacity; preference is saved | Implemented |
| Local pack hand-off | Select .mcpack, .mcaddon, .mcworld or .zip and hand it to Minecraft/Android | Implemented, device-dependent |
| CurseForge catalogue | Search UI and optional backend connection are present | Requires a configured backend and approved API key |
| In-game modules | Capability information is shown, but gameplay toggles are not connected to Minecraft | Not implemented |
| Isolated game instances | Separate app-level instance management is not available yet | Planned |

The current app metadata is **0.3.0** (versionName in app/build.gradle.kts). A successful debug build means the Android project compiled; it does not prove that every Minecraft version, device, import flow or backend deployment has been tested.

## Screens and features

### Home

- Displays the selected installed Minecraft edition and detected version.
- Provides a launch action for that edition.
- Links to the Instances and CurseForge areas.
- Offers a shortcut to personalize the launcher wallpaper.

### Instances

The current Instances screen is an **installed-edition selector**. It checks for these Android package variants:

- Stable: com.mojang.minecraftpe
- Preview: com.mojang.minecraftpe.preview
- Beta: com.mojang.minecraftpe.beta

Only editions already installed on the device can be selected and launched. The screen does not download arbitrary game versions or create isolated copies of Minecraft.

### CurseForge

The native catalogue screen includes:

- Search by project name.
- Content category selection (Add-ons, Maps, Texture Packs, Scripts and Skins).
- Popular / latest sorting and refresh.
- Project details and supported file download flow.
- A fallback link to the official Bedrock add-ons catalogue when the service is unavailable.

Catalogue access is optional. It requires the separately hosted Node.js service described below, together with an approved CurseForge API key configured on that server. The app does not contain the private API key.

### Local pack and world import

The Android document picker accepts:

- .mcpack
- .mcaddon
- .mcworld
- .zip

Rabbit Client asks Android to open the selected file with Minecraft or another compatible app. Android/Minecraft may show an app chooser or an import confirmation. **This is a hand-off workflow, not a promise of silent installation into Minecraft's private storage.**

### Personalization

- Obsidian-inspired dark interface.
- Selectable accent colour presets.
- Custom launcher wallpaper.
- Adjustable glass panel transparency.
- Saved appearance preferences and wallpaper access.

### Modules

The Modules area documents planned in-game capabilities and explains their integration requirements. Auto Sprint, Night Vision, Hitbox Visualizer, Replay and a movable HUD are **not active features** in this preview. A launcher-only toggle cannot change gameplay: each module needs a verified Android-compatible integration with the running Bedrock game.

## Important Android limitations

Rabbit Client intentionally distinguishes an installed-game launcher from a managed game runtime.

1. **No separate Minecraft installation yet.** Launching Stable or Preview opens that installed Android package. It does not create a second data directory.
2. **No automatic world or pack isolation.** The existing Minecraft app retains ownership of its private data.
3. **No in-game injection or gameplay module runtime.** The Windows/desktop approaches used by other clients cannot simply be copied into an Android app.
4. **No silent private-storage modification.** File imports use Android's supported document and URI-sharing flows.
5. **No universal version guarantee.** Detecting a version or successfully opening the game does not prove that a particular in-game integration is compatible.

Rabbit Client must not claim isolated instances, working gameplay modules, or universal Minecraft-version support until those capabilities are implemented and tested on real devices.

## Architecture

    Rabbit Client
    ├── Android app
    │   ├── Kotlin
    │   ├── Jetpack Compose + Material 3
    │   ├── Installed Minecraft package detection
    │   ├── Android launch-intent hand-off
    │   ├── Document picker and file hand-off
    │   ├── Theme, wallpaper and glass preferences
    │   └── CurseForge catalogue client
    │
    └── Optional backend
        ├── Node.js HTTP service
        ├── CurseForge API proxy
        ├── Server-side API-key handling
        └── Search and supported-file download endpoints

The Android UI and the optional catalogue service are separate. The backend is not required to build the app, select an installed edition, change appearance, or hand a local pack to Minecraft.

## Requirements

| Component | Requirement |
| --- | --- |
| Android Studio | A recent version with Android SDK support |
| Android SDK | Compile SDK 35; Android build tools 35.0.0 for the CI configuration |
| Minimum Android version | API 28 (Android 9) |
| Java | JDK 17 |
| Kotlin | Kotlin Android and Compose compiler plugins from the project Gradle configuration |
| Optional backend | Node.js 20 or newer |

The project uses Android application ID **com.rabbitclient**.

## Build the Android app

1. Clone the repository:

       git clone https://github.com/khargosh-area/rabbitclient.git
       cd rabbitclient

2. Open the project in Android Studio and allow Gradle to sync.
3. Install the Android SDK platform/build tools required by the project.
4. Build and run the app on an Android device or emulator:

       gradle testDebugUnitTest assembleDebug

5. The debug APK is produced at:

       app/build/outputs/apk/debug/app-debug.apk

The repository's GitHub Actions workflow runs the backend JavaScript syntax check, Android unit tests and debug APK build on pushes to main, pull requests targeting main, and manual workflow dispatch. The APK is uploaded as a workflow artifact; it is not automatically a Play Store release.

## Optional CurseForge service

The service lives in backend/. Its purpose is to keep the private CurseForge API key on a server rather than inside the APK.

### API routes

| Method | Route | Purpose |
| --- | --- | --- |
| GET | /health | Service health check |
| GET | /api/curseforge/search?query=...&category=Add-ons&sort=Popular | Search the verified Bedrock catalogue |
| GET | /api/curseforge/download/:projectId | Resolve a supported downloadable file |

### Run locally

1. Open a terminal in the backend directory:

       cd backend
       npm install

2. Configure the API key in your local shell. Obtain approval and a key from the [CurseForge for Studios console](https://console.curseforge.com/).

   Linux/macOS:

       export CURSEFORGE_API_KEY="YOUR_APPROVED_KEY"
       npm start

   PowerShell:

       $env:CURSEFORGE_API_KEY="YOUR_APPROVED_KEY"
       npm start

3. The service listens on port 8787 by default. Check it with:

       http://localhost:8787/health

4. In Rabbit Client, open **Settings → CurseForge**, enter the backend base URL and tap **Save and test service**. For a physical phone, use a reachable HTTPS host rather than the Android emulator's 10.0.2.2 address.

The backend reads CURSEFORGE_API_KEY from the process environment. It does not automatically load a local .env file. Do not commit secrets.

### Deploy the backend

Deploy the backend/ directory as a Node.js web service:

- **Build command:** npm install
- **Start command:** npm start
- **Secret environment variable:** CURSEFORGE_API_KEY
- **Port:** supplied by the host through PORT, or defaults to 8787

Use the deployed HTTPS base URL in the app (for example, https://your-service.example), without adding /api to the end.

The service calls the official CurseForge API. It checks for a verifiable Bedrock category and fails closed when it cannot confirm one; it should not silently return Java-only projects as Bedrock add-ons. Only .mcpack, .mcaddon, .mcworld and .zip files are eligible. The Android download flow only accepts HTTPS and enforces a 256 MB transfer limit before handing the file to Minecraft/Android.

Read [backend/README.md](backend/README.md) and [backend/.env.example](backend/.env.example) for the service-specific details.

## Version and compatibility policy

The target range is **Minecraft Bedrock 1.21.90 and newer**, including future releases only when the relevant launch flow and integrations are verified. This is a target range, not a statement that every version has been tested.

Minecraft's version numbering moved to year-based releases in 2026 (for example, 26.x). Rabbit Client should therefore parse the detected version instead of assuming that all future releases will use a 1.22-style number.

Compatibility has separate levels:

- **Detected:** Android reports an installed Minecraft package/version.
- **Launcher hand-off:** Rabbit Client can open that installed package.
- **In-game integration:** a specific feature has been tested against that game build.

Unknown future versions should be labelled as detected but not yet verified. See [docs/minecraft-version-support.md](docs/minecraft-version-support.md).

## Roadmap

The following are goals, not promises that these features are already available:

- A real managed Rabbit Instance with its own verified runtime and separate data.
- Safe world, resource-pack and behaviour-pack backup/export/import workflows.
- A supported account flow with secure token storage.
- Better Bedrock add-on discovery and import feedback.
- Diagnostics and clearer compatibility reporting.
- An original in-game integration layer, only if a legitimate and technically verified Android path is available.
- A modular feature registry and configurable HUD after the game integration exists.

The separate-instance architecture and its acceptance checklist are documented in [docs/instance-architecture.md](docs/instance-architecture.md).

## Security and privacy

- Keep the CurseForge API key on the backend host. Never paste it into Kotlin source, a checked-in configuration file, or the Android APK.
- Use HTTPS for a deployed catalogue service and its downloads.
- The backend uses a lightweight in-memory rate limit; it is not a replacement for proper host-level access controls and abuse protection.
- Pack downloads are restricted to HTTPS URLs returned by the official API and supported file extensions.
- Local file selection uses Android's Storage Access Framework and URI permissions.
- Do not ask users to paste Microsoft passwords or access tokens into the app.
- Do not silently overwrite Minecraft worlds, account data, or installed game files.

## Credits and licensing

**Project creator:** [Khargoshh](https://github.com/khargosh-area)  
**AI development assistance:** ChatGPT

Rabbit Client has its own Android interface and implementation. Research into other launchers and clients is for understanding workflows and platform constraints; it does not imply affiliation, endorsement, or identical functionality. Preserve all third-party license notices when using third-party code or assets. Do not redistribute Minecraft assets or other copyrighted material without permission.

The supplied desktop-source investigation notes identify an AGPL-3.0-licensed source archive. Review the applicable license obligations before reusing any code from that archive. See the research notes in docs/ for evidence boundaries and platform-specific findings.

## Contributing

Issues and carefully scoped pull requests are welcome. Before adding a feature:

1. Identify whether it belongs in the Android launcher, the optional backend, or a future in-game runtime.
2. Use Android-supported APIs for package discovery, document access and launch hand-off.
3. Keep secrets out of the repository and APK.
4. Add tests for version parsing, input validation and backend behavior where applicable.
5. Test user-visible flows on a real Android device.
6. Mark features as planned or unavailable until their behavior is demonstrated.

Useful project notes:

- [Compatibility matrix](docs/compatibility-matrix.md)
- [Instance architecture and acceptance checklist](docs/instance-architecture.md)
- [Minecraft version support policy](docs/minecraft-version-support.md)
- [Flarial static inspection](docs/flarial-static-inspection.md)
- [Android investigation notes](docs/rea-investigation-followup-2-android-map.md)
