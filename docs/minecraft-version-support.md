# Minecraft Bedrock version support policy

Rabbit Client's target compatibility range is **Minecraft Bedrock/MCPE 1.21.90 and newer, including future releases where the required APIs/integration remain compatible**.

This is a compatibility goal, not a claim that every version has already been tested. Minecraft's version numbering moved to year-based versions in 2026 (for example, 26.x), so the app must not assume that the newest version will always start with 1.21.

## Version handling requirements

- Detect the installed Minecraft package and version at runtime where Android allows it.
- Treat 1.21.90 as the minimum supported baseline for the requested range.
- Do not hardcode a finite list of every future version. Use a parsed version model and a capability/compatibility check.
- Support package variants only when the user has that variant installed and its launch flow is actually verified.
- If a version is unknown or newer than the last tested version, show **Detected — not yet verified** rather than claiming it is fully supported.
- Separate launcher compatibility (can identify/launch the installed game) from in-game integration compatibility (verified modules work).
- Display the detected game version and compatibility status on the home screen.
- Add regression tests for 1.21.90, a later 1.21.x release, a 26.x release, malformed version strings, and unknown future versions.
- Never silently edit or replace the user's Minecraft installation to force compatibility.

## Official release references

- Minecraft Bedrock 1.21.90: https://feedback.minecraft.net/hc/en-us/articles/37393460002957-Minecraft-Bedrock-Edition-1-21-90-Chase-the-Skies
- Minecraft Bedrock 1.21.92: https://feedback.minecraft.net/hc/en-us/articles/37486954069645-Minecraft-1-21-92-Bedrock
- Minecraft Bedrock 26.0 notes that Mojang switched to year-based version numbering in 2026: https://feedback.minecraft.net/hc/en-us/articles/43274629736717-Minecraft-Bedrock-Edition-26-0-Changelog

## Status wording

Until tested on a real device and game build, use wording like **Target range: 1.21.90+** and **Runtime compatibility: not verified**. Do not use a green "supported" badge solely because the version string is within range.
