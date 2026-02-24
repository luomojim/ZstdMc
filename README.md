# ZstdMC (Forge 1.20.1)

ZstdMC is a Forge mod that replaces Minecraft's vanilla packet compression/decompression handlers with Zstandard (Zstd) to reduce multiplayer bandwidth usage.

## Current Target

- Loader: Forge
- Minecraft: 1.20.1
- Forge: 47.x
- Java: 17

## What This Mod Changes

- Replaces `Connection.setupCompression` pipeline handlers:
  - Vanilla `compress` -> `ZstdCompressionEncoder`
  - Vanilla `decompress` -> `ZstdCompressionDecoder`
- Works for:
  - Dedicated server
  - Integrated server (Open to LAN)

## Installation

1. Put the mod on both server and client.
2. Set `network-compression-threshold` in `server.properties` to `128` or higher (recommended `256`).
3. For LAN play, both the host and all joining clients must load this mod.

## Verification In Game

- Press F3 and check right-side debug text for `[Zstd Metrics]`.
- Run admin commands:
  - `/zstd status`
  - `/zstd reset`
  - `/zstd top10`

## Build

- `gradlew.bat buildAll`
  - `fit` jar: regular jar (no dependency shading)
  - `fat` jar: includes local `libs/zstd-jni-1.5.7-6.jar`

In most environments, prefer the `fat` jar.
