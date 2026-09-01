# swing-gui Spec Delta

## ADDED Requirements

### Requirement: Dock 图标

GUI 模式运行期间，系统 SHALL 通过标准 Taskbar API 将 macOS Dock 图标设置为应用图标资源 `dock_logo.png`（以 128px + 256px@2x 双档多分辨率图像呈现，等比缩放不拉伸变形）。GUI 启动初期 SHALL NOT 出现 Java 默认咖啡杯图标——系统 SHALL 在 AWT 初始化前将图标资源拷贝到临时文件（每次启动全新拷贝）并预置给 JVM。环境不支持 Taskbar 或预置失败时 SHALL 静默跳过/降级，SHALL NOT 崩溃或弹错。终端模式与托盘守护进程 SHALL NOT 设置 Dock 图标。

#### Scenario: 运行期 Dock 展示应用图标

- **WHEN** 用户以 `--gui` 启动加密狗
- **THEN** macOS Dock 中该应用显示 dock_logo.png 图标（清晰不模糊、非方形等比缩放、非 Java 默认咖啡杯图标）

#### Scenario: 启动期无咖啡杯闪现

- **WHEN** 用户以 `--gui` 启动加密狗并观察启动全过程
- **THEN** Dock 图标从应用出现起即为 dock_logo.png，全程不显示 Java 默认咖啡杯图标

#### Scenario: Taskbar 不可用静默降级

- **WHEN** 运行环境不支持 Taskbar（如无桌面环境的服务器）
- **THEN** 系统静默跳过 Dock 图标设置，GUI 其余功能正常，不崩溃、不弹错

#### Scenario: 终端与托盘模式不设置

- **WHEN** 用户以终端模式（不带 `--gui`/`--tray`）或托盘守护模式（`--tray`）运行
- **THEN** 该进程不设置 Dock 图标，行为与引入 Dock 图标前一致
