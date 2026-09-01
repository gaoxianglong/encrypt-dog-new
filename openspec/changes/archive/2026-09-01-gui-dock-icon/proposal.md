# GUI 运行期 Dock 图标

## Why

GUI 运行时 macOS Dock 显示的是 Java 默认咖啡杯图标，与应用的紫色品牌形象不符（菜单栏托盘已用 logo）。用户提供了专门的 Dock 图标资源 `dock_logo.png`（1254×1254 方形 PNG），希望运行期 Dock 显示该图标。

## What Changes

- GUI 启动时通过 `java.awt.Taskbar.setIconImage` 设置 Dock 图标（Java 9+ 标准 API，macOS 上即 Dock 图标），使用 `dock_logo.png`。设置时机在 EDT 上、窗口可见之后（视觉验收发现：macOS AWT 在窗口首次显示时才关联 Dock，过早设置会被激活过程重置）。
- **消除启动期咖啡杯闪现**：`Starter.main` GUI 路径在 AWT 初始化前把 dock_logo.png 拷贝到临时文件（每次启动全新拷贝，防临时文件被删；deleteOnExit 自动清理）并预置 `apple.awt.application.icon` 内部属性，JVM 挂 Dock 时即显示应用图标。
- 图标以 128px + 256px@2x 双档 `BaseMultiResolutionImage` 呈现（复用 `LogoUtil` 等比缩放，防 retina 模糊）。
- `UiConstants` 新增 `DOCK_LOGO_RESOURCE` 常量；资源已由 pom 整目录打包自动进 jar，无构建配置改动。
- 环境不支持 Taskbar 或预置属性失效时静默降级（无 Dock 图标或退回运行时设置，不影响任何功能），SHALL NOT 崩溃或弹错。
- 仅 GUI 模式设置；终端模式与托盘守护进程不受影响。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

- `swing-gui`: 新增需求「Dock 图标」——GUI 运行期 Dock 图标 SHALL 以 dock_logo.png 呈现（128/256 双档、等比缩放），Taskbar 不可用时静默降级。

## Impact

- 受影响代码：`EncryptDogGui.launch`（设置调用）、`UiConstants`（资源常量）；复用 `LogoUtil`。
- 新增资源：`src/main/resources/dock_logo.png`（用户已提供）。
- 无 API 变更、无终端模式变更、无依赖变更。
