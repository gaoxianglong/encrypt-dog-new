# 设计：菜单栏常驻托盘（双进程）

## Context

`Starter.main` 按 `--gui` 路由到 `EncryptDogGui.launch`，否则走 picocli 终端路径。GUI 渲染开销重：`ParticlePanel` 以 16ms 帧率持续动画（FRAME_DELAY_MS=16），执行页另有 33ms 脉冲时钟，窗口可见期间持续消耗 CPU；窗口 `EXIT_ON_CLOSE`。项目仅支持 macOS（终端校验拒绝非 Mac）。logo 资源 `logo.png` 随 jar 发布，`LogoUtil.loadImage(resource, size)` 可等比缩放。JDK 支持 `MultiResolutionImage`（9+）与 `var`。

动机与范围见 proposal.md；需求见 specs/menu-bar-tray/spec.md 与 specs/swing-gui/spec.md。

## Goals / Non-Goals

**Goals:**

- 双进程隔离：渲染重的 GUI 退出即完全释放 CPU/内存，常驻守护零渲染零动画（用户定案动机）
- 图标在 GUI 退出后常驻，Open 唤醒、Quit 全退
- 单例语义：任何入口唤醒已有 GUI 而非重复开窗

**Non-Goals:**

- 不做登录自启（launchd，用户明确按"首次执行后出现"语义）
- 不隐藏 Dock 图标（Java AWT 限制，需原生辅助程序，不做）
- 不动终端模式任何行为
- 不专门适配 Windows/Linux（SystemTray API 天然跨平台，但仅验收 macOS）

## Decisions

### D1：双进程架构（守护 + GUI），否决单进程常驻与原生助手

用户定案理由：GUI 渲染消耗 CPU。单进程常驻（关窗仅隐藏）要求所有动画定时器（粒子 16ms、脉冲 33ms、缓动）在隐藏时精确停启，隐藏窗口仍存在误耗风险，且 1g 堆配置的 JVM 常驻内存高；原生 Swift 助手（NSStatusItem）最省资源但为 Maven 单体项目引入第二工具链与双语言维护，收益不成比例。双进程：GUI 真退出（EXIT_ON_CLOSE 不动），守护 `-Xmx64m` 只挂静态图标。

### D2：同一 jar 增加 `--tray` 运行模式，守护由 GUI 自动拉起

`Starter` 增加 `--tray` 路由（内部参数，用户无需感知）：不建窗口、不解析 picocli，仅执行托盘守护逻辑。GUI 启动时探测守护端口，缺则拉起：`ProcessBuilder(java.home/bin/java, -Xmx64m, -jar, <self-jar>, --tray)`——数组传参避免 shell 转义问题。self-jar 路径取 `ProtectionDomain.getCodeSource().getLocation()`（fat jar 有效），解析失败回退 `java.class.path` 并记日志。拉起后轮询端口最多 3s 等待就绪。

### D3：两个 localhost socket + 魔数握手

仅绑定 127.0.0.1，命令为明文短行（show/quit），无敏感数据：

```
守护端口 32345: 存活检查 + 握手 (守护侧 ServerSocket)
GUI 端口 32346: 单例锁 + 命令接收 (GUI 侧 ServerSocket)
  命令: "show" -> SwingUtilities.invokeLater(toFront + requestFocus)
        "quit" -> frame.dispose() (EXIT_ON_CLOSE 自然退出)
握手: 连接后先交换魔数 "ENCRYPTDOG1", 不匹配即视为陌生进程 -> 降级路径
```

托盘行为：Open → 连 GUI 端口，成功发 "show"；失败（无 GUI）→ spawn `--gui` 进程。Quit → 连 GUI 端口发 "quit"（失败忽略）→ 移除图标 → 守护退出。守护被杀无心跳补偿——自愈依赖 GUI 每次启动的确保逻辑（D6）。

### D4：GUI 单例与启动顺序

GUI 启动序列：先尝试 bind GUI 端口 32346 → 成功即自己是唯一 GUI，随后 ensureDaemon（探测 32345 + 握手，缺则 spawn + 轮询）→ 展示窗口；bind 失败 → 连接 32346 发 "show" → 自己 `System.exit(0)`。二次启动因此在 ensureDaemon 之前就短路退出，避免重复拉起与双窗。

### D5：托盘图标与菜单

`TrayIcon` 使用 `BaseMultiResolutionImage(LogoUtil 22px, LogoUtil 44px)`（retina 双档），`setImageAutoSize(false)` 防缩放模糊；tooltip "Encrypt Dog"；`PopupMenu` 两项：Open Encrypt Dog / Quit。macOS 深色菜单栏下紫色 logo 辨识度为视觉验收点，偏暗时后续变更出亮色变体。

### D6：守护生命周期与竞态

守护启动即 bind 32345（bind 失败 = 已有守护或陌生占用；前者正常退出，后者握手可辨）。GUI 拉起的守护进程与 GUI 无父子生命周期绑定（ProcessBuilder 独立进程，GUI 退出守护不随退）。GUI 每次启动的 ensureDaemon 构成守护的"自愈"机制：被杀后下次启动自动重拉。终端模式路径零托盘代码调用。

### D7：优雅降级矩阵

| 情况 | GUI 行为 |
| --- | --- |
| 守护端口被陌生进程占（握手失败） | 正常启动与使用，仅无托盘，不崩溃不阻塞 |
| GUI 端口被陌生进程占 | 单例保护失效（可能双窗），正常使用；记录为已知限制 |
| 守护 spawn 失败（java 路径/JAR 解析失败） | GUI 正常启动，记日志无托盘 |

## Risks / Trade-offs

- [守护常驻多一个 JVM（~50-80MB RSS）] → 相对主 GUI 1g 配置与渲染 CPU 是净收益；这是用户选定的权衡。
- [每次 Open 冷启动 GUI 1-2 秒] → 双进程固有代价；单例路径（已运行）为瞬时前台。
- [端口硬编码冲突] → 魔数握手区分本程序/陌生进程 + D7 降级矩阵兜底。
- [守护与 GUI 同时被强杀后菜单栏残留图标] → AWT TrayIcon 随进程销毁自动移除，无残留风险。
- [深色菜单栏图标可见性] → 视觉验收点；亮色变体作为后续微调项。

## Migration Plan

无数据/持久化变更。升级路径：用户以新版 jar 首次 `--gui` 启动即自动获得托盘；老行为（无托盘）在守护拉起失败时按降级矩阵保留。
