# 彻底移除菜单栏托盘功能

## Why

用户决定不再需要菜单栏托盘功能（无论 .app/dmg 形态还是 jar 运行形态）：守护常驻进程、菜单栏图标、Open/Quit 交互都不再使用。与其留着维护成本，彻底删除。

## What Changes

- **删除** `gui/interfaces/swing/tray/` 整个包（`TrayDaemon`、`TrayIpc`：守护进程、socket 协议、候选链等全部代码）。
- **回退** `Starter.java`：移除 `--tray` 路由、`TRAY_OPTION` 常量、TrayDaemon import 与参数解析排除（保留 `preinstallDockIcon`——Dock 图标功能不受影响）。
- **回退** `EncryptDogGui.java`：移除单例锁（`acquireSingleton`/`serveCommands`/`guiServer`/`frame` 静态字段）、`ensureDaemon` 调用与 tray 相关 import，窗口创建回退为局部变量（保留 `installDockIcon`——Dock 图标功能不受影响）。
- **单例行为一并删除**（用户确认）：二次 `--gui` 启动不再唤醒旧窗口，可能开两个窗口——与托盘引入前一致。
- 本地端口 32345/32346 不再使用。
- `build-mac.sh` 与 .app/dmg 打包流程不受影响（打包产物只是不再有托盘行为）。
- spec：`menu-bar-tray` 能力**整体退役**（全部 5 条需求 REMOVED，`.openspec.yaml` 已声明 `retire_capabilities: true`）；`swing-gui` 回退「启动参数路由」（移除 `--tray` 路由与守护确保条款；因 MODIFIED 语义约束不允许删场景，「托盘守护模式启动」保留场景名、内容改写为"`--tray` 不再被识别，走终端 picocli 路径"）并清理「Dock 图标」需求中的托盘守护表述（不设置范围改为仅终端模式，场景名保留）。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

- `swing-gui`: 「启动参数路由」回退为无托盘版本（移除 `--tray` 路由与守护确保条款、删除「托盘守护模式启动」场景）；「Dock 图标」需求清理托盘守护表述（不设置范围改为仅终端模式）。
- `menu-bar-tray`: 能力退役——全部 5 条需求 REMOVED（Reason + Migration），主 spec 整体删除。

## Impact

- 受影响代码：删除 `tray/` 包 2 个类；修改 `Starter.java`、`EncryptDogGui.java`（回退）。
- 行为变化：无菜单栏图标、无守护进程、无二次启动单例；Dock 图标、终端模式、打包脚本均不受影响。
- 无 API/依赖变化。
