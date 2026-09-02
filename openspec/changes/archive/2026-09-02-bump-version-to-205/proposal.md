# 版本号收敛至 2.0.5

## Why

pom 构建版本已由用户升级为 2.0.5，但 README 与主 spec 场景示例中仍残留硬编码的 2.0.4 字样，与真实构建产物（`encryptdog-2.0.5.jar`）及运行时展示（banner "version: 2.0.5"、GUI 版权 "v2.0.5"）不一致。

## What Changes

- README.md 共 7 处 `2.0.4` 更新为 `2.0.5`：版本 badge、两处 `dog` 别名示例、wget 下载链接（v2.0.4 → v2.0.5）、两条 GUI 启动命令示例、终端 banner 输出示例。
- `openspec/specs/branding/spec.md` 4 处场景/需求示例文案中的 `encryptdog-2.0.4.jar` / `EncryptDog-2.0.4.dmg` 更新为 2.0.5（纯示例文本，SHALL 语义不变）。
- `openspec/specs/swing-gui/spec.md`「版本号与构建版本一致」场景示例文案中的 `2.0.4` 更新为 `2.0.5`（纯示例文本，SHALL 语义不变）。
- 不改动 pom.xml（已是 2.0.5）、dog.properties（`${project.version}` 过滤注入）、UiConstants/BannerInfo（读 properties 自动）、build-mac.sh（动态取 pom 版本）——这些位置随 pom 自动生效。
- 不改动 `openspec/changes/archive/` 下历史归档中的版本字样（历史记录保留原样）。

## Capabilities

无能力变更：所有 spec 更新均为示例文案（需求语义不变），本变更以 `skip_specs: true` 声明。

## Impact

- 受影响文件：README.md、openspec/specs/branding/spec.md、openspec/specs/swing-gui/spec.md（示例文本）。
- 行为影响：无运行时行为变化；重新打包后产物名、banner 版本、GUI 版本展示自动为 2.0.5（此前已随 pom 生效）。
- 无 API/依赖变化。
