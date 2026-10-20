# Rabbit Client Instance Architecture

## Goal

Rabbit Client should eventually offer two clearly separated launch targets:

1. **Minecraft (Normal)** — open the user's existing official Minecraft installation without changing its worlds, packs, settings, or sign-in state.
2. **Rabbit Instance** — a managed, isolated game environment with its own supported sign-in flow, game data, worlds, resource/behavior packs, settings, and Rabbit integration.

## Important Android constraint

An Android launcher cannot safely create a genuinely separate Minecraft data directory merely by changing its own UI or storing files in its private app folder. Android app sandboxing prevents Rabbit Client from directly reading/writing Minecraft's private data. Opening the installed Minecraft package launches that same installation and does not create a second instance.

A real isolated instance requires a compatible, legally distributable game/runtime integration or another supported installation mechanism that can own and launch separate data. Do not claim isolation until that runtime is implemented and tested on-device. Do not silently overwrite or migrate the user's existing Minecraft data.

## Planned design

- **Instance registry:** stable instance ID, display name, runtime identifier, version metadata, and storage root owned by the supported runtime.
- **Normal launch:** use Android's launch intent for the detected installed package; never modify its data as a side effect.
- **Rabbit instance launch:** enabled only after a real isolated runtime is installed and verified. Never route this button to the normal package while labeling it isolated.
- **Accounts:** account records must be scoped to the appropriate runtime/instance. Use the supported Microsoft/Xbox authentication flow, secure token storage, and explicit sign-out. Never ask users to paste passwords into Rabbit Client.
- **Pack imports:** use Android's Storage Access Framework to let users select .mcpack, .mcaddon, and supported archives. Validate file type/size, copy to app-owned staging storage, and preserve the original file. Apply/import packs only through the selected instance's verified runtime.
- **Worlds and backups:** instance-scoped storage, explicit export/import, safe overwrite confirmation, and recoverable backups.
- **Compatibility:** distinguish detected version, launcher support, runtime support, and verified in-game module support. Unknown future versions must not be marked compatible without tests.
- **Modules:** module toggles are not gameplay features until a verified in-game adapter consumes their settings.

## Acceptance checklist

- [ ] Normal Minecraft still launches and its data is untouched.
- [ ] Rabbit instance has a distinct, verified data root.
- [ ] A world created in one instance does not appear in the other unless explicitly imported.
- [ ] A pack imported into Rabbit is scoped to Rabbit and does not alter normal Minecraft.
- [ ] Accounts and tokens are scoped and stored securely.
- [ ] Backups can be restored and export files can be inspected.
- [ ] Install, launch, sign-in, import, and update flows pass on a real Android device.
- [ ] Version compatibility is tested against the declared Bedrock versions.

## Current status

The current app can detect some installed Minecraft package names and launch the detected app. It does **not** yet create a separate game installation, isolate worlds/packs, provide Microsoft sign-in, or run in-game modules. The instance UI must keep these distinctions explicit.
