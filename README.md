## ServerSeekerNMC (Fabric, no Meteor)

This is a fork of the original ServerSeeker mod by Ogmur, adapted to run as a pure Fabric client mod without Meteor.

- Original project: https://github.com/Ogmur/ServerSeeker  
- Credits: All original authors/maintainers of the ServerSeeker project.

### What’s different in this fork
- No Meteor dependency (works with Fabric Loader only)
- New Multiplayer UI buttons:
  - Find servers: query ServerSeeker API and add/join results
  - Get players: show recent players for the selected server
- In‑game Settings screen to configure API Base URL and API Key

### Install
1) Ensure Fabric Loader for your Minecraft version is installed  
2) Download the built jar from Releases and drop it into your `mods` folder  
3) Launch Minecraft → Multiplayer → “Find servers” (and optionally “Settings” to set API URL/key)

Notes:
- The public docs/API may be temporarily unavailable. If you see 404/403, set your own API Base URL/API Key via Settings when available.

### Build from source
Requirements: JDK 21

Windows (PowerShell):
```
./gradlew.bat clean remapJar
```

Artifacts: `build/libs/server-seeker-<version>.jar`

### License
This repository keeps the original project license (MPL-2.0). See `LICENSE`.
