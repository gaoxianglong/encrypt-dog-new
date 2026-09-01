# 设计：GUI 运行期 Dock 图标

## Context

GUI 模式经 `EncryptDogGui.launch` 启动（FlatLaf 安装 → 滚动条主题化 → EDT 建窗）。Dock 图标当前为 Java 默认咖啡杯。用户提供 `src/main/resources/dock_logo.png`（1254×1254 方形 PNG，透明通道），pom 整目录打包进 jar classpath。`LogoUtil.loadImage(resource, size)` 可等比缩放并缓存。JDK 21。

动机与范围见 proposal.md；需求见 specs/swing-gui/spec.md。

## Goals / Non-Goals

**Goals:**

- GUI 运行期 Dock 显示 dock_logo.png（128/256 双档防模糊）
- 不可用环境静默降级，零崩溃风险

**Non-Goals:**

- 不改终端模式与 `--tray` 守护进程（守护是否有 Dock 图标属现状，不在本变更范围）
- 不做原生 .icns/App Bundle 打包（纯 Java 运行形态不适用）

## Decisions

### D1：`java.awt.Taskbar.setIconImage`，否决 `com.apple.eawt.Application`

JDK 9+ 标准导出 API，无需 `--add-exports`（eawt 包未导出，要模块参数才能用，且是旧 API）。macOS 上 `Taskbar.setIconImage` 即 Dock 图标；`Taskbar.isTaskbarSupported()` 守卫，不支持时静默跳过。

### D2：128px + 256px@2x 双档 BaseMultiResolutionImage

Dock 图标以 ~128×128 逻辑像素渲染：`LogoUtil.loadImage(DOCK_LOGO_RESOURCE, 128)` + `(…, 256)` 装进 `BaseMultiResolutionImage` 再 `setIconImage`。dock_logo.png 原图 1254px 远大于目标尺寸，缩比采样无模糊风险；`LogoUtil` 等比缩放语义与既有标题栏 logo 一致。

### D3：Taskbar 设置时机 = EDT 上、窗口可见之后（视觉验收修正）

初版在"FlatLaf 后、建窗前"调用 `setIconImage`，视觉验收不通过——macOS AWT 在窗口**首次显示时**才把 java 进程关联到 Dock，此前的设置被激活过程重置为咖啡杯。修正为 `frame.setVisible(true)` 之后的 EDT 上调用。设置失败（异常/不支持）捕获后继续，不打日志噪声（环境性问题非错误）。仅 GUI 路径调用——终端与守护零接触。

### D4：常量 `UiConstants.DOCK_LOGO_RESOURCE = "dock_logo.png"`

与既有 `LOGO_RESOURCE = "logo.png"` 并列，命名一致。

### D5：启动期预置属性消除咖啡杯闪现（视觉验收反馈迭代）

运行时 Taskbar 设置无法覆盖 JVM 挂 Dock 到窗口可见之间的 1-2 秒——期间显示 JVM 默认咖啡杯。`Starter.main` GUI 路径在 AWT 初始化前：把 dock_logo.png 从 classpath 拷贝到临时文件并 `System.setProperty("apple.awt.application.icon", 路径)`（macOS JDK 在 AWT 初始化时读取该内部属性作为 Dock 图标）。**每次启动全新拷贝**（用户要求，防临时文件被删：createTempFile 路径唯一 + deleteOnExit 自动清理）。属性为 JDK 内部未文档化 API——失效时静默退化为运行时 Taskbar 设置（方案 A 行为），无崩溃风险。注意：Starter 中不引用 UiConstants 常量，避免其 Color 字段类加载提前触碰 AWT 类。

## Risks / Trade-offs

- [Taskbar 在无桌面环境返回 null/抛异常] → `isTaskbarSupported()` + try-catch 静默降级，GUI 功能零影响。
- [Dock 图标与菜单栏托盘图标用不同资源文件的风格差异] → dock_logo.png 由用户专门设计，属有意为之；验收确认两者视觉协调。
- [`apple.awt.application.icon` 内部属性未来 JDK 移除] → 属性失效自动退回运行时 Taskbar 设置（仅恢复咖啡杯闪现，功能无损）。
- [临时图标文件管理] → 每次启动全新拷贝 + deleteOnExit；强杀进程残留孤儿文件为 1.4MB 级，可忽略。

## Migration Plan

纯运行时视觉设置，无数据/持久化变更；回滚即删除 launch 中设置调用与常量。
