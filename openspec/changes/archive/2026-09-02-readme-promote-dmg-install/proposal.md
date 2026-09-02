# README 安装方式改为 dmg 优先

## Why

v2.0.5 已发布 dmg 安装包（`EncryptDog-2.0.5.dmg`），而 README 的 install 节目前以源码构建与 jar 为主，dmg 完全未提及——与"GUI 为主界面"的产品形态脱节。用户定案：重点推荐 dmg 方式，其余安装方式保留并支持。

## What Changes

- README.md `### install` 节重写为三段式：`macOS App (recommended)` 置顶主推（wget 下载 `EncryptDog-2.0.5.dmg` → 拖入 Applications → 双击启动即进入 GUI）；`jar (also supported)` 保留（wget `encryptdog-2.0.5.jar` + `dog` 别名，GUI/终端两用）；`build from source` 保留（git clone + mvn package）。
- `### gui mode` 小节开头补一句说明：dmg 安装用户直接启动 .app 即进入 GUI，`--gui` 启动命令适用于 jar 形态。
- 按用户后续决定，删除 `### gui mode` 中冗长的 Interface guide 整段（含逐字段操作说明与托盘说明条目，共 10 条 bullet），GUI 使用说明不再逐字段罗列。
- 按用户决定不提及 Gatekeeper/未签名提示。
- 下载地址使用 v2.0.5 Release 真实资产：`https://github.com/gaoxianglong/encrypt-dog-new/releases/download/v2.0.5/EncryptDog-2.0.5.dmg`。

## Capabilities

无能力变更：纯文档调整，本变更以 `skip_specs: true` 声明。branding spec「文档品牌引用」需求（README jar 引用为 `encryptdog-<version>.jar`）不受影响——jar 小节保留。

## Impact

- 受影响文件：README.md（install 节与 gui mode 开头一句）。
- 行为影响：无运行时行为变化。
- 无 API/依赖变化。
