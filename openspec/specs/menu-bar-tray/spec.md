# menu-bar-tray Specification

## Purpose

在 macOS 菜单栏提供加密狗的常驻托盘图标：GUI 首次运行后图标常驻（除非手动退出），GUI 窗口关闭后图标仍在，用户可通过图标唤醒 GUI 或彻底退出，常驻守护为无渲染、无动画的轻量独立进程。

## Requirements

### Requirement: 托盘守护进程生命周期

系统 SHALL 在 GUI 模式启动时确保托盘守护进程运行：探测本地守护端口，未运行时以低内存配置（`-Xmx64m`）拉起同一 jar 的 `--tray` 模式守护进程；守护已在运行时 SHALL NOT 重复拉起。守护 SHALL 仅挂载菜单栏图标，SHALL NOT 创建窗口、SHALL NOT 运行任何渲染动画或定时器。终端模式（不带 `--gui`）SHALL NOT 启动或触碰托盘守护，行为与引入托盘前完全一致。

#### Scenario: 首次 GUI 启动拉起守护

- **WHEN** 用户首次以 `--gui` 启动且无守护进程运行
- **THEN** GUI 自动拉起守护进程，菜单栏出现 logo 图标，随后 GUI 窗口正常显示

#### Scenario: 守护已在时不重复拉起

- **WHEN** 守护进程已在运行（图标已在菜单栏），用户再次以 `--gui` 启动
- **THEN** 系统不重复拉起守护，菜单栏图标保持唯一

#### Scenario: 终端模式无托盘

- **WHEN** 用户以不带 `--gui`/`--tray` 的方式启动（如 `java -jar dog.jar -e -s xxx -k yyy`）
- **THEN** 不出现托盘图标、不启动守护进程，终端行为与引入托盘前完全一致

### Requirement: 菜单栏图标与菜单

托盘图标 SHALL 使用应用 logo（22pt 菜单栏显示尺寸，SHALL 提供 2x 高清变体避免 retina 模糊）；图标菜单 SHALL 包含两项：`Open Encrypt Dog`（唤醒/拉起 GUI）与 `Quit`（独立退出按钮）。图标 SHALL 常驻菜单栏直到用户点击 Quit。

#### Scenario: 图标展示

- **WHEN** 守护进程运行期间查看 macOS 菜单栏
- **THEN** 菜单栏展示加密狗 logo 图标，图标清晰不模糊，悬停/点击行为正常

#### Scenario: 菜单项完整

- **WHEN** 用户点击菜单栏图标
- **THEN** 弹出菜单仅包含 `Open Encrypt Dog` 与 `Quit` 两项，无其它残留项

### Requirement: 唤醒与单例

托盘 `Open Encrypt Dog` SHALL 在 GUI 未运行时拉起新 GUI 进程（`--gui` 模式）；GUI 已在运行时 SHALL 将已有窗口带到前台，SHALL NOT 重复开窗。用户直接二次以 `--gui` 启动时语义相同（单例）。

#### Scenario: Open 拉起 GUI

- **WHEN** 托盘守护运行且 GUI 未运行，用户点击 `Open Encrypt Dog`
- **THEN** 系统拉起新 GUI 进程并显示主窗口

#### Scenario: Open 带前台不重复开窗

- **WHEN** GUI 窗口已在运行（可能被遮挡），用户点击 `Open Encrypt Dog`
- **THEN** 已有窗口被带到前台，不新增窗口、不新增进程

#### Scenario: 二次启动带前台

- **WHEN** GUI 已在运行，用户再次直接以 `--gui` 启动
- **THEN** 新实例将已有窗口带到前台后自行退出，不出现两个窗口

### Requirement: 退出语义

GUI 窗口点击 ✕ SHALL 仅退出 GUI 进程（关闭行为与引入托盘前一致），菜单栏图标 SHALL 保留。托盘 Quit SHALL 在 GUI 仍在运行时先通知其关闭，随后移除图标并退出守护进程（全部退出）。

#### Scenario: 关闭窗口图标保留

- **WHEN** 用户点击 GUI 窗口 ✕
- **THEN** GUI 进程退出、窗口与 Dock 图标消失，菜单栏 logo 图标保留且可继续唤醒

#### Scenario: 托盘 Quit 全退

- **WHEN** 用户点击托盘菜单 Quit（无论 GUI 是否正在运行）
- **THEN** GUI 若在运行则被关闭，菜单栏图标消失，守护进程退出

### Requirement: 自愈与端口冲突降级

守护进程被外部终止后，下次 GUI 启动 SHALL 自动重新拉起守护。托盘端口被陌生程序占用（握手失败）时，GUI SHALL 正常启动并可用，仅无托盘功能，SHALL NOT 崩溃或拒绝启动。

#### Scenario: 守护被杀后自愈

- **WHEN** 守护进程被外部终止（如活动监视器强杀），用户再次以 `--gui` 启动
- **THEN** GUI 自动重新拉起守护，菜单栏图标恢复

#### Scenario: 端口冲突优雅降级

- **WHEN** 托盘端口被与本程序无关的进程占用（通信握手失败）
- **THEN** GUI 正常启动与使用，仅菜单栏无图标，不崩溃、不弹错、不阻塞启动
