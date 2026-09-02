# 单进程菜单栏托盘

## Why

用户希望在 macOS 菜单栏常驻 EncryptDog 图标：窗口关闭后仍可通过图标唤回，干活中在菜单栏实时查看进度，完成后收到通知。上次（2026-09-01）的双进程托盘方案因"GUI 渲染持续烧 CPU（粒子动画 16ms 帧率）"而选择守护进程隔离；现在粒子与电路轨迹背景均已退役、回归纯紫色静态渐变，剩余动画 Timer 全部瞬态，单进程常驻的成本趋近于零——双进程所需的守护进程、双端口 IPC、魔数握手、自愈与降级矩阵全部不再需要。

## What Changes

- 新增 `gui/interfaces/swing/tray/TrayManager`：单进程内挂载 macOS 菜单栏图标（`logo.png` 品牌标志，22pt + 44pt@2x 双档，与标题栏同源），无 `--tray` 参数、无守护进程、无窗口渲染。
- 托盘菜单二态切换：空闲（Show / Reveal last output / Quit，tooltip "EncryptDog"）与干活中（Show / 禁用状态行 "Encrypting 3/12 · 41%" / Quit，tooltip 同步进度）。
- **BREAKING**（GUI 行为变化）：窗口 ✕ 关闭由退出进程改为仅隐藏窗口，任务继续后台执行；真正的退出只有托盘 Quit 与 Cmd+Q（空闲直接退出；干活中弹出主题化确认浮层，确认后直接退出进程）。
- Dock 点击唤回隐藏窗口（`com.apple.eawt` reopen 回调，保留 Dock 图标不变）。
- 二次启动单例：jar 形态重复 `--gui` 启动时，新进程唤醒已有实例（含隐藏窗口）后自行退出，菜单栏不出现第二个图标。
- Reveal last output：打开最近一次成功输出文件所在目录并选中该文件；从未成功输出过时菜单项禁用。
- 完成通知：窗口隐藏期间任务完成时弹出系统通知（`TrayIcon.displayMessage`）；完成提示音沿用 core 既有 `FinishedListener` 播放。
- `Starter.java` 与终端模式零改动；打包流程（build-mac.sh、jpackage）不引入 `LSUIElement`，Dock 图标保留。

## Capabilities

### New Capabilities

- `menu-bar-tray`: 单进程菜单栏托盘能力——托盘生命周期与降级、二态菜单与 tooltip、窗口关闭/恢复/退出语义、干活状态与完成通知、Reveal last output、二次启动单例。

### Modified Capabilities

- `swing-gui`: 「启动参数路由」新增重复启动唤醒场景；「Dock 图标」新增 Dock 点击唤回隐藏窗口场景。

## Impact

- 受影响代码：新增 `gui/interfaces/swing/tray/TrayManager`；修改 `EncryptDogGui.java`（托盘挂载、单例锁、退出钩子）、`EncryptDogFrame.java`（`HIDE_ON_CLOSE`、Dock reopen 回调、干活状态通知 tray）。
- 复用既有组件：`ThemedConfirmDialog`（退出确认浮层）、`LogoUtil.loadImage`（图标缩放）、`OperationProgressDTO`/`OperationResultDTO`（进度与结果数据源）；无新依赖。
- 行为变化：✕ 不再退出进程（**BREAKING**，README 需更新说明）；二次启动不再可能开两个窗口；托盘不可用时 ✕ 保持退出语义（降级矩阵）。
- 终端模式、密钥存储、打包脚本零影响。
