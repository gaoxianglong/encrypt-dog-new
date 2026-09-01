# 实现任务：GUI 运行期 Dock 图标

## 1. 常量与设置接线

- [x] 1.1 `UiConstants` 新增 `DOCK_LOGO_RESOURCE = "dock_logo.png"` 常量（与 LOGO_RESOURCE 并列、命名一致）。验证：`mvn compile` 通过。
- [x] 1.2 `EncryptDogGui.launch` 在 EDT 上、窗口可见后调用 Dock 图标设置（`installDockIcon`）：`Taskbar.isTaskbarSupported()` 守卫 → `LogoUtil.loadImage(DOCK_LOGO_RESOURCE, 128/256)` 双档装 `BaseMultiResolutionImage` → `Taskbar.getTaskbar().setIconImage(...)`，整体 try-catch 静默降级。验证：`mvn compile` 通过；初版"建窗前调用"因 macOS AWT 在窗口首次显示时才关联 Dock 而被重置（视觉验收不通过），已修正时机。

## 2. 构建与回归

- [x] 2.1 `mvn package` 确认 `dock_logo.png` 已打包进 jar（`jar tf` 检查 classpath 根存在该资源）。验证：解包或列表含 dock_logo.png。
- [x] 2.2 启动 GUI：Dock 显示 dock_logo.png 图标（清晰不模糊、非咖啡杯），GUI 关闭后 Dock 图标随进程消失。验证：与 spec 场景「运行期 Dock 展示应用图标」一致。
- [x] 2.3 托盘守护（`--tray`）进程 Dock 表现与改动前一致；托盘 Open/Quit、二次启动单例不受影响。验证：与「终端与托盘模式不设置」场景一致。
- [x] 2.4 终端模式回归：不带 `--gui`/`--tray` 执行一次加/解密，行为与改动前完全一致。

## 3. 启动期咖啡杯闪现消除（视觉验收反馈迭代）

- [x] 3.1 `Starter.main` GUI 路径增加 `preinstallDockIcon()`：AWT 初始化前把 dock_logo.png 拷贝到临时文件（**每次启动全新拷贝**，createTempFile 路径唯一防被删，deleteOnExit 自动清理）并设置 `apple.awt.application.icon` 属性，失败静默降级为运行时 Taskbar 设置。验证：`mvn compile` 通过。
- [x] 3.2 启动期无咖啡杯闪现：启动 GUI 全过程 Dock 直接显示 dock_logo.png，不再出现 Java 默认咖啡杯。验证：用户验收通过（与「启动期无咖啡杯闪现」场景一致）。
