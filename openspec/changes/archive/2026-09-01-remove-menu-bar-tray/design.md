# 设计：彻底移除菜单栏托盘功能

## Context

托盘功能由 `gui/interfaces/swing/tray/` 包（TrayDaemon/TrayIpc）承载，并经 `Starter.java`（`--tray` 路由）与 `EncryptDogGui.java`（单例锁/守护确保/命令接收）接线。Dock 图标（`preinstallDockIcon`/`installDockIcon`）与托盘无依赖关系（仅共享了"进程启动"时机），必须保留。`build-mac.sh` 打包流程不依赖托盘代码。用户确认：单例唤醒一并删除，回退为托盘引入前行为。

动机与范围见 proposal.md；需求见 specs/menu-bar-tray/spec.md 与 specs/swing-gui/spec.md。

## Goals / Non-Goals

**Goals:**

- 托盘全部代码、路由、端口、spec 能力零残留
- 保留：Dock 图标、终端模式、打包脚本、GUI 全部既有功能

**Non-Goals:**

- 不动 Dock 图标任何逻辑（预置属性与运行时 Taskbar 设置均保留）
- 不动加密核心/终端模式/表单/执行页
- 不动 `build-mac.sh`（.app/dmg 打包继续可用，产物只是没有托盘行为）

## Decisions

### D1：手工 hunk 级回退，而非 git revert

`Starter.java`/`EncryptDogGui.java` 中托盘与 Dock 图标改动交织（同文件、同方法区域），整文件 `git revert` 会连带丢掉 Dock 图标代码。采用手工回退：仅移除托盘相关行（tray 包 import、`--tray` 路由、单例锁块、`ensureDaemon`、静态 frame/guiServer、serveCommands），保留 preinstall/installDockIcon。回退后与"托盘引入前"形态的差异点仅剩 Dock 图标逻辑——符合预期。

### D2：端口与进程零残留

32345（守护）/32346（GUI 单例）不再绑定；无任何新线程（serveCommands 的 accept 线程随删除消失）；无守护 spawn。GUI 生命周期完全回到单进程：启动建窗 → ✕ 即退出（EXIT_ON_CLOSE 不变）。

### D3：能力退役与 spec 清理

`menu-bar-tray` 全部 5 条需求 REMOVED（每条附 Reason/Migration），`.openspec.yaml` 已声明 `retire_capabilities: true`——同步时主 spec 整体删除、目录移除。`swing-gui`：「启动参数路由」回退到无托盘文本；MODIFIED 语义约束不允许删场景，「托盘守护模式启动」保留场景名、内容改写为"`--tray` 不再被识别，走终端 picocli 路径"；「Dock 图标」清理托盘守护表述（需求文本改为仅终端模式不设置，「终端与托盘模式不设置」场景名保留、内容去除 `--tray` 引用）。

### D4：验证策略

- 编译 + 终端回归（加/解密往返）
- jar 形态：`--gui` 启动无守护进程（32345 无监听）、无菜单栏图标；**二次启动开两窗为预期行为**（用户确认的回退语义）
- Dock 图标保持（截屏/用户验收）
- `./build-mac.sh` 重新打包后 .app 正常启动、无守护进程、Dock 图标正常

## Risks / Trade-offs

- [手工回退遗漏 tray 相关行] → 以编译错误（未用 import/变量）与 grep 双重检查（tray/TrayIpc/TrayDaemon/3234[56] 关键词零命中）。
- [二次启动开两窗可能让用户意外] → 用户已确认接受；提案与 spec 均记录该回退语义。
- [已归档的托盘变更留在 archive 历史中] → 正常——archive 是历史记录，不删除。
- [.app 分发场景失去托盘差异化] → 用户明确不需要；.app 仅剩 GUI + Dock 图标，自包含性反而更强（不再依赖外部 java 拉守护）。

## Migration Plan

无数据/配置迁移。运行形态：`dog --gui`/`java -jar`/`.app` 三者行为一致（GUI + Dock 图标，无托盘无单例）。旧版本若已在菜单栏留幽灵图标，重启后消失（守护进程退出即清）。
