# Rabbit Client — CurseForge API service

This Node.js service keeps the official CurseForge API key off the Android APK. It provides:

- GET /api/curseforge/search?query=...&category=Add-ons&sort=Popular
- GET /api/curseforge/download/:projectId
- GET /health

## Why this is a backend

The official CurseForge for Studios API requires an approved x-api-key header. Never put this key in Kotlin source, a checked-in config, or the APK. Apply for access at https://console.curseforge.com/ and review https://docs.curseforge.com/rest-api/.

## Run locally

Requirements: Node.js 20 or newer.

1. Copy .env.example to .env and set CURSEFORGE_API_KEY locally. Do not commit .env.
2. Run npm start from this directory with the variable exported in your shell.
3. For Android emulator testing, the base URL can be http://10.0.2.2:8787. A physical phone needs an address reachable from that phone; use HTTPS for a hosted service.

The backend does not read .env automatically. Export the variable in your terminal or configure it in your process manager.

## Host it

Deploy this directory as a Node web service on a trusted host, with:

- Build command: npm install
- Start command: npm start
- Secret environment variable: CURSEFORGE_API_KEY

Then paste the deployed HTTPS base URL (for example, https://your-service.example) into Rabbit Client → CurseForge → API service URL. Do not include /api at the end.

## Bedrock and downloads

The service queries the official API for a Bedrock category before listing results. If the API account does not expose a category that can be verified as Bedrock, the service fails closed rather than silently showing Java-only projects. A project's latest supported file must be a .mcpack, .mcaddon, .mcworld, or .zip. The direct download URL is requested from CurseForge's official API instead of scraping the website.

The Android app downloads the selected file to its private cache and opens it through Android's URI-grant flow. Minecraft may display an import confirmation or Android may show an app chooser, depending on the device and installed Minecraft build. This is not a guarantee of silent import into Minecraft's private storage.

## Safety and limits

- The in-memory rate limit is a lightweight safeguard, not a substitute for host-level protection.
- Only HTTPS direct download URLs returned by CurseForge are passed to the client.
- API key values are read only from process.env; they are never returned by this service.
- Do not run an unauthenticated public instance with a personal key without reviewing provider rate limits and adding appropriate host-level protection.
