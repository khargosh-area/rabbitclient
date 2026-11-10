# 🐇 Rabbit Client

<p align="center">
  <img src="app/src/main/res/drawable/rabbit_logo.xml" alt="Rabbit Client logo" width="112" />
</p>

<h1 align="center">Rabbit Client</h1>
<p align="center"><strong>A native Android companion launcher for Minecraft Bedrock Edition.</strong></p>

<p align="center">
  <a href="https://github.com/khargosh-area/rabbitclient/actions/workflows/android.yml"><img src="https://github.com/khargosh-area/rabbitclient/actions/workflows/android.yml/badge.svg" alt="Android Build"></a>
  <img src="https://img.shields.io/badge/Android-9%2B-3DDC84?logo=android&logoColor=white" alt="Android 9+">
  <img src="https://img.shields.io/badge/Kotlin-1F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Version-0.3.0%20Preview-orange" alt="Preview">
</p>

Rabbit Client is an Android application built with **Kotlin, Jetpack Compose and Material 3**. It gives Minecraft Bedrock players a dedicated place to choose an installed game edition, launch it, hand supported add-on/world files to Minecraft, explore Bedrock content through an optional CurseForge service, and personalize the launcher interface.

> [!IMPORTANT]
> **Rabbit Client is a work-in-progress companion launcher, not a replacement Minecraft game client.** The current build opens Minecraft editions already installed on the device. Features that need code running inside Minecraft are not advertised as working until a real Android-compatible integration exists.

---

## Contents

- [Project vision](#-project-vision)
- [Current release and status](#-current-release-and-status)
- [Features](#-features)
- [Screens](#-screens)
- [Supported files](#-supported-files)
- [How the launcher works](#-how-the-launcher-works)
- [Android limitations](#-important-android-limitations)
- [Technology stack](#-technology-stack)
- [Project structure](#-project-structure)
- [Requirements](#-requirements)
- [Build the Android app](#-build-the-android-app)
- [CurseForge backend setup](#-curseforge-backend-setup)
- [Compatibility policy](#-minecraft-compatibility-policy)
- [Roadmap](#-roadmap)
- [Security and privacy](#-security-and-privacy)
- [Research and technical notes](#-research-and-technical-notes)
- [Credits and licensing](#-credits-and-licensing)
- [Contributing](#-contributing)

## 🎯 Project vision

The goal is to build a polished, genuinely useful Android companion for Minecraft Bedrock with a premium dark interface and clear separation between launcher features and in-game features.

Design principles:

- Native Android experience rather than a website wrapped in an APK.
- Obsidian-inspired dark UI, restrained glass effects and mint/accent colour options.
- Custom wallpaper and adjustable panel transparency.
- Real installed-game detection and launch hand-off.
- Safe file selection and import hand-off using Android APIs.
- Optional content discovery without exposing private API credentials in the APK.
- No fake toggles, pretend instances or unsupported compatibility claims.

## 📌 Current release and status

| Item | Value |
| --- | --- |
| App name | Rabbit Client |
| Current project version | 0.3.0 Preview |
| Android application ID | `com.rabbitclient` |
| Minimum Android version | Android 9 / API 28 |
| Compile / target SDK | 35 / 35 |
| Language | Kotlin |
| UI toolkit | Jetpack Compose + Material 3 |
| Java target | 17 |
| Project state | Preview / active development |

A successful Gradle or CI build confirms that the project compiles. It does **not** mean every Minecraft release, Android device, add-on, backend deployment or in-game feature has been tested.

## ✨ Features

### Implemented in the current app

- **Installed edition detection:** checks for Minecraft Stable, Preview and Beta package variants when Android makes them available.
- **Edition selection:** choose an edition already installed on the device and retain the selected package.
- **Launch hand-off:** opens the selected Minecraft package through Android's package launch intent.
- **Home dashboard:** shows the selected edition/version and provides access to launcher areas.
- **Custom wallpaper:** choose an image through Android's document picker; retain the URI permission for later use.
- **Theme presets:** switch the launcher accent/theme preset.
- **Glass opacity:** adjust translucent panel appearance.
- **Local file hand-off:** select supported Bedrock pack/world files and send them to Minecraft or another compatible app.
- **CurseForge client UI:** search and browse through the optional backend service.
- **Honest module status:** the Modules page explains which gameplay features still need a verified in-game integration.

### Not implemented yet

These are **not active gameplay features** in the current preview:

- Auto Sprint
- Night Vision
- Hitbox Visualizer
- Replay recording / Replay Mod
- Movable in-game HUD
- A separate, isolated Minecraft installation
- Independent worlds, accounts and pack storage per Rabbit instance
- A built-in Bedrock runtime that downloads and launches arbitrary game versions

A launcher screen or toggle alone cannot change Minecraft gameplay. Each in-game feature needs a compatible runtime bridge and device/game-version testing.

## 🧭 Screens

### Home
The dashboard displays the currently selected installed edition, its detected version and a launch action. It also provides shortcuts to Instances, CurseForge and wallpaper customization.

### Instances
In this preview, **Instances means installed-edition selection**, not independent game installations.

The app checks these package IDs:

| Edition | Android package |
| --- | --- |
| Minecraft Stable | `com.mojang.minecraftpe` |
| Minecraft Preview | `com.mojang.minecraftpe.preview` |
| Minecraft Beta | `com.mojang.minecraftpe.beta` |

Only installed packages can be selected and launched.

### CurseForge
The catalogue screen supports project search, category selection, sorting/refresh, project details and supported-file download hand-off. It uses the optional Node.js backend so the private CurseForge API key stays off the Android device.

If the backend is unavailable or not configured, the app can direct users to the official Bedrock add-ons catalogue instead.

### Modules
This screen describes planned gameplay integrations and their technical requirements. It does not pretend that unsupported features are active.

### Settings
Launcher appearance preferences include theme/accent presets, wallpaper and glass opacity. CurseForge service configuration is also available from Settings.

### About
Project and app information, credits and project links.

## 📦 Supported files

The local file picker accepts:

| Extension | Intended content |
| --- | --- |
| `.mcpack` | Bedrock resource/behaviour/skin pack |
| `.mcaddon` | Bedrock add-on bundle |
| `.mcworld` | Bedrock world |
| `.zip` | Supported archive hand-off |

Rabbit Client uses Android's document picker and URI-sharing flow to hand the selected file to Minecraft or another compatible app. Minecraft/Android may show an app chooser or import confirmation. This is **not** silent installation into Minecraft's private storage and does not guarantee every archive will be accepted by every game version.

## 🚀 How the launcher works

1. Android reports which Minecraft package variants are installed and visible to Rabbit Client.
2. Rabbit Client shows the detected editions and versions.
3. The user selects an installed edition.
4. On Launch, the app asks Android to open that package.
5. For a local add-on/world file, the user selects it through the document picker and Rabbit Client hands it to a compatible app.

Rabbit Client does not replace or silently modify the official Minecraft installation during this flow.

## ⚠️ Important Android limitations

Android applications are sandboxed. A launcher cannot create a truly separate Minecraft data directory merely by storing a profile name or changing its own UI.

- Launching Stable or Preview opens that installed Android package.
- The installed Minecraft app continues to own its private worlds, packs and account data.
- A second launcher profile is not proof of an isolated game instance.
- Desktop Windows game hooks, DirectX renderers and DLL-based modules cannot simply be copied into an Android app.
- File hand-off through Android does not grant unrestricted access to Minecraft's private storage.
- Detecting a version or opening the game does not prove in-game module compatibility.

A real Rabbit Instance needs a supported, legally distributable Android-compatible runtime with its own verified data ownership and launch path. It must pass the acceptance checklist in [docs/instance-architecture.md](docs/instance-architecture.md) before being described as isolated.

## 🧰 Technology stack

| Layer | Technology |
| --- | --- |
| Android app | Kotlin |
| UI | Jetpack Compose |
| Design components | Material 3 |
| Android APIs | Android SDK / AndroidX |
| Build system | Gradle Kotlin DSL |
| Java compatibility | JDK 17 |
| Optional API service | Node.js |
| Catalogue provider | Official CurseForge for Studios API |
| CI | GitHub Actions |

## 📁 Project structure

```text
rabbitclient/
├── .github/
│   └── workflows/
│       └── android.yml        # Android CI build
├── app/
│   ├── src/main/
│   │   ├── java/com/rabbitclient/
│   │   │   ├── MainActivity.kt
│   │   │   └── GameVersion.kt
│   │   └── res/               # Rabbit branding and Android resources
│   ├── src/test/              # Unit tests
│   └── build.gradle.kts
├── backend/
│   ├── server.js              # CurseForge API proxy
│   ├── package.json
│   ├── .env.example
│   └── README.md
├── docs/                      # Compatibility, architecture and research notes
├── build.gradle.kts
├── gradle.properties
└── settings.gradle.kts
```

## 🛠 Requirements

- Android Studio with Android SDK support.
- Android SDK Platform 35 and Build Tools 35.0.0 for the current CI setup.
- JDK 17.
- Android 9 / API 28 or newer for the app.
- Node.js 20+ only if running the optional backend.

## 🔨 Build the Android app

Clone the repository:

```bash
git clone https://github.com/khargosh-area/rabbitclient.git
cd rabbitclient
```

Open the project in Android Studio and allow Gradle to sync. Then run:

```bash
gradle testDebugUnitTest assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The GitHub Actions workflow runs the backend JavaScript syntax check, Android unit tests and debug APK build on pushes to `main`, pull requests targeting `main`, and manual workflow dispatch. The resulting APK is uploaded as a workflow artifact. It is not automatically a Play Store release or a signed production release.

## 🌐 CurseForge backend setup

The optional service is in `backend/`. It keeps the approved CurseForge API key on the server rather than embedding it in the APK.

### API routes

| Method | Route | Description |
| --- | --- | --- |
| GET | `/health` | Health check |
| GET | `/api/curseforge/search?query=...&category=Add-ons&sort=Popular` | Search catalogue |
| GET | `/api/curseforge/download/:projectId` | Resolve a supported project file |

### Run locally

```bash
cd backend
npm install
```

Obtain API access through the [CurseForge for Studios console](https://console.curseforge.com/). Set the approved key in your shell:

Linux/macOS:

```bash
export CURSEFORGE_API_KEY="YOUR_APPROVED_KEY"
npm start
```

PowerShell:

```powershell
$env:CURSEFORGE_API_KEY="YOUR_APPROVED_KEY"
npm start
```

The service listens on port `8787` by default. Check `/health` on the local service. In Rabbit Client, open **Settings → CurseForge**, enter the backend base URL and use **Save and test service**.

### Deploy

Deploy the `backend/` directory as a Node.js web service:

- Build command: `npm install`
- Start command: `npm start`
- Secret environment variable: `CURSEFORGE_API_KEY`
- Port: host-provided `PORT`, or `8787` by default

Use the deployed HTTPS base URL in the app, without appending `/api`.

The backend uses the official CurseForge API. It checks for a verifiable Bedrock category and fails closed if it cannot confirm one. It accepts only supported Bedrock file extensions (`.mcpack`, `.mcaddon`, `.mcworld`, `.zip`) and HTTPS download URLs. The Android transfer flow enforces a 256 MB limit before handing the file to Android/Minecraft.

See [backend/README.md](backend/README.md) and [backend/.env.example](backend/.env.example) for service-specific configuration.

## 🎮 Minecraft compatibility policy

The target range is **Bedrock 1.21.90 and newer**, but this is a target range rather than a guarantee that every version has been tested.

Minecraft adopted year-based version numbering in 2026 (for example, 26.x). Rabbit Client should parse detected version strings instead of assuming all future versions use a 1.22-style number.

Compatibility is reported in separate levels:

- **Detected:** Android reports an installed Minecraft package/version.
- **Launcher hand-off:** Rabbit Client can open that installed package.
- **In-game integration:** a specific feature has been tested on that exact game build.

Unknown future versions should be labelled detected but not yet verified. See [docs/minecraft-version-support.md](docs/minecraft-version-support.md).

## 🗺 Roadmap

The following are development goals, not claims about the current preview:

- [ ] A real managed Rabbit Instance with a verified runtime and separate data.
- [ ] Safe world, resource-pack and behaviour-pack backup/export/import.
- [ ] Supported account flow with secure token storage.
- [ ] Better Bedrock add-on discovery and clearer import feedback.
- [ ] Diagnostics and compatibility reporting.
- [ ] Original Android-compatible in-game integration, if a legitimate and technically verified path is available.
- [ ] Modular feature registry and configurable HUD after a real game integration exists.
- [ ] Device-level testing across supported Android and Bedrock versions.

The isolated-instance architecture and its acceptance criteria are documented in [docs/instance-architecture.md](docs/instance-architecture.md).

## 🔐 Security and privacy

- Never place the CurseForge API key in Kotlin source, checked-in config or the APK.
- Store backend credentials only in the hosting provider's secret environment.
- Use HTTPS for deployed catalogue requests and downloads.
- The backend's in-memory rate limit is only a lightweight safeguard; production deployments need host-level abuse protection.
- Only official API-returned HTTPS download URLs are passed to the Android client.
- File selection uses Android's Storage Access Framework and URI permissions.
- Do not ask users to paste Microsoft passwords or access tokens into Rabbit Client.
- Do not silently overwrite Minecraft worlds, account data or installed game files.
- Validate archive contents before any future extraction or restore workflow is added.

## 📚 Research and technical notes

The `docs/` directory contains investigation notes and implementation boundaries:

- [Compatibility matrix](docs/compatibility-matrix.md)
- [Instance architecture and acceptance checklist](docs/instance-architecture.md)
- [Minecraft version support policy](docs/minecraft-version-support.md)
- [Flarial static inspection](docs/flarial-static-inspection.md)
- [Android investigation map](docs/rea-investigation-followup-2-android-map.md)
- [Module lifecycle and HUD notes](docs/rea-investigation-followup-3-module-lifecycle.md)
- [Launcher implementation comparison notes](docs/rea-levi-flarial-implementation.md)

Research notes distinguish evidence from assumptions. The presence of a class name or UI label in another project is not proof that the same feature is available in Rabbit Client.

## 🙌 Credits and licensing

- **Project creator:** [Khargoshh](https://github.com/khargosh-area)
- **AI development assistance:** ChatGPT

Rabbit Client has its own Android interface and implementation. Studying other launchers/clients is for understanding workflows and platform constraints; it does not imply affiliation, endorsement or identical functionality.

Preserve third-party license notices when integrating third-party code. Do not redistribute Minecraft assets or other copyrighted material without permission. The supplied desktop-source investigation identifies an AGPL-3.0-licensed source archive; review all applicable license obligations before reusing any code from it.

## 🤝 Contributing

Issues and scoped pull requests are welcome. Before adding a feature:

1. Decide whether it belongs in the Android launcher, optional backend or a future in-game runtime.
2. Use Android-supported APIs for package discovery, document access and launch hand-off.
3. Keep credentials out of the repository and APK.
4. Add tests for version parsing, input validation and backend behavior where appropriate.
5. Test user-visible flows on a real Android device.
6. Label a feature as planned/unavailable until its behavior is demonstrated.
7. Do not advertise an isolated instance or working gameplay module without passing its acceptance tests.

---

<p align="center"><strong>Built with care by Khargoshh + ChatGPT 🐇</strong></p>
