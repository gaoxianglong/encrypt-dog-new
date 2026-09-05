# menu-bar-tray Spec Delta

## MODIFIED Requirements

### Requirement: 单进程托盘挂载与降级

GUI 模式启动时，系统 SHALL 以单进程方式在 macOS 菜单栏挂载应用图标（`logo.png` 品牌标志资源，与标题栏同源，22pt 与 44pt@2x 双档多分辨率呈现，等比缩放不拉伸变形，图标内容以约 82% 内框渲染、在槽位内保留约 18% 的视觉留白与 macOS 菜单栏原生图标惯例一致，槽位尺寸不变），不引入独立启动参数、守护进程或第二个 JVM；终端模式 SHALL NOT 挂载菜单栏图标。系统托盘不可用或图标资源缺失时 SHALL 静默降级为无托盘运行，GUI 其余功能正常，且该场景下窗口 ✕ SHALL 保持退出进程语义。

#### Scenario: GUI 模式启动挂载图标

- **WHEN** 用户以 `--gui` 方式启动加密狗
- **THEN** macOS 菜单栏出现应用图标（清晰不模糊、与 Dock 图标同源、视觉大小与相邻菜单栏原生图标相当不偏大），tooltip 显示 "EncryptDog"，进程为单进程形态

#### Scenario: 终端模式不挂载图标

- **WHEN** 用户以终端模式（不带 `--gui`）运行
- **THEN** 菜单栏不出现任何图标，行为与托盘引入前完全一致

#### Scenario: 托盘图标视觉留白

- **WHEN** 用户对比菜单栏中的加密狗图标与相邻原生图标
- **THEN** 加密狗图标内容不顶满 22pt 槽位、四周保留约 18% 视觉留白，视觉大小与 macOS 菜单栏图标惯例一致

#### Scenario: 托盘不可用静默降级

- **WHEN** 运行环境不支持系统托盘（`SystemTray.isSupported()` 为 false）或图标资源缺失
- **THEN** 系统不挂载托盘、不崩溃、不弹错，GUI 其余功能正常，窗口 ✕ 关闭退出进程
