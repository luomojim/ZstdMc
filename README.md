# ZstdMC (Forge 1.20.1)

## 中文说明

### 项目简介
ZstdMC 是一个 Forge 1.20.1 模组，用 Zstandard (Zstd) 替换 Minecraft 原版网络压缩与解压流程，从而在多人和局域网联机中减少网络流量占用。

### 版本与环境
- Minecraft: `1.20.1`
- Forge: `47.x`
- Java: `17`（运行时可用 17+）

### 功能特性
- 替换 `Connection.setupCompression` 的压缩与解压处理器。
- 支持专用服务器与局域网（Open to LAN）场景。
- 提供调试统计（F3 右侧显示 `[Zstd 压缩统计]`）。

### 安装说明
1. 将模组放入客户端与服务端（或局域网主机与加入方）`mods` 文件夹。
2. 在 `server.properties` 中设置：
   - `network-compression-threshold=128`（建议 `256`）。

### 包类型说明
- `fat`：默认推荐，内置 `zstd-jni`，多数环境可直接使用。
- `fit`：不内置 `zstd-jni`，用于兼容场景。

一般情况下请优先使用 `fat`。  
如果启动出现报错（例如与其他模组重复提供 zstd 相关类导致冲突），再切换到 `fit` 版本。

### 常见问题
- 提示“无法加载有效的 resourcepackinfo”：
  - 旧版构建缺少 `pack.mcmeta`，请使用本仓库重新构建后的新版本。

## English

### Overview
ZstdMC is a Forge 1.20.1 mod that replaces Minecraft's vanilla packet compression/decompression with Zstandard (Zstd), reducing bandwidth usage in multiplayer and LAN sessions.

### Target Environment
- Minecraft: `1.20.1`
- Forge: `47.x`
- Java: `17` (runtime 17+)

### Features
- Replaces compression handlers in `Connection.setupCompression`.
- Works on dedicated servers and integrated LAN hosts.
- Provides debug metrics on F3 (`[Zstd Metrics]`).

### Installation
1. Put the mod on both client and server (or LAN host and all joining clients).
2. Set this in `server.properties`:
   - `network-compression-threshold=128` (recommended `256`).

### Artifact Types
- `fat`: recommended by default, bundles `zstd-jni`, and works out-of-the-box in most environments.
- `fit`: does not bundle `zstd-jni`, intended for compatibility cases.

In general, use `fat` first.  
If startup fails (for example, duplicate zstd classes provided by another mod), switch to `fit`.

### Troubleshooting
- If you see `invalid resourcepackinfo`:
  - Older jars were missing `pack.mcmeta`. Use a newly built jar from this repository.
