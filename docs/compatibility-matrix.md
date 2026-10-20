# Rabbit Client compatibility matrix from supplied Flarial artifacts

This matrix is a planning aid based on the supplied Android APK's printable strings/class descriptors and the supplied Windows source archive's file tree/headers. It is not proof of runtime behavior.

| Rabbit Client area | Evidence found | Confidence | Rabbit Client decision |
| --- | --- | --- | --- |
| Launcher/game handoff | Android DEX names `MinecraftActivity`, `MinecraftPreloadService`, and background service classes | Medium-high for component existence; behavior untraced | Keep launch adapter separate from UI; test actual launch on supported devices |
| Minecraft storage | Android strings mention worlds, resource packs, behavior packs, skin packs, and Shizuku permission flow | Medium-high for referenced flows | Use Android document/provider APIs and explicit permission UX; no broad storage assumptions |
| Backup/restore | `.flbackup` strings and dedicated request, inspection, preflight, progress and conflict-policy class descriptors | High for dedicated subsystem existence | Separate backup service; validate archives before extraction; build round-trip tests |
| Migration/recovery | Dedicated migration activities, providers and transfer service descriptors | High for subsystem existence | Treat migration as its own staged workflow with recovery/rollback states |
| Microsoft/Xbox account flow | `MsftLoginActivity` and Microsoft/Xbox SDK strings | Medium-high for account integration presence | Use official supported authentication only; do not imitate token internals |
| In-game module framework | Windows source includes a common module abstraction, settings, keybind actions, and lifecycle state | High for source design | Design an independent Kotlin module registry; do not copy Windows-specific implementation wholesale |
| HUD rendering | Windows source includes `HUDModule`, rendering events and movable HUD modules | High for source design | Define a capability-based HUD model and persisted layout; rendering adapter must be provided by a verified in-game runtime |
| ClickGUI | Windows source contains ClickGUI and element files | High for source tree presence | Build Rabbit Client's own original interface; do not conflate Android launcher UI with in-game ClickGUI |
| Runtime hooks | Windows source tree has game/input/render/visual hooks and DirectX-specific render paths | High for Windows source architecture | Not portable to Android; do not claim this is the Android integration mechanism |
| Lua scripting | Windows source contains scripting manager and Lua 5.4.7 files | High for subsystem presence | Optional future feature only; not required for launcher v0.1 |
| FPS/coordinates/CPS/keystrokes/potion HUD/fullbright/sprint/zoom/FOV | Corresponding Windows module folders/files exist | Medium for feature implementation presence; runtime behavior not tested | Reimplement only after a supported game capability exists and each feature is independently verified |
| Reach-related feature family | Windows tree contains names such as `OpponentReach` and `ReachCounter` | High for names in archive | Explicitly exclude all Reach-related features from Rabbit Client, including UI, registry, configs, docs and tests |

## Platform boundary

The Windows source CMake project uses MSVC-specific flags, Windows rendering APIs, DirectX and a shared-library target. The Android artifact is a Kotlin/Java Android APK with a separate launcher activity/service/data-management structure. There is no evidence in the supplied archive that the Windows hook implementation can run on Android.

## Evidence labels

- **High** means the named file/class/string is directly present in the supplied artifact.
- **Medium** means a string/class name strongly suggests a role but method flow has not been traced.
- No row claims a feature was executed or passed a functional test.

## Next validation gates

1. Decode the Android manifest and inspect method bodies using REA/JADX in a supported environment.
2. Trace the game-launch handoff and any bridge used after launch.
3. Implement and test launcher features that do not depend on an in-game bridge (for example, local preferences and safe archive inspection).
4. Keep in-game modules disabled/marked unsupported until an actual bridge is identified and tested.
