# 设计：单进程菜单栏托盘

## Context

动机与范围见 proposal.md；需求见 specs/menu-bar-tray/spec.md 与 specs/swing-gui/spec.md。

现状关键约束：

- `Starter.main` 仅按 `--gui` 路由到 `EncryptDogGui.launch`，终端路径不触碰任何 GUI 代码。
- `EncryptDogFrame` 当前 `EXIT_ON_CLOSE`（EncryptDogFrame.java:133）；执行流：`startOperation` 起 daemon worker 线程 → `appService.execute(form, files, listener)` → EDT 上 `finishOperation(result)` / `operationFailed(msg)` 收尾。
- 进度数据：core 的 `ProgressEvent.progress` 为当前文件百分比字符串（如 `"41%"`）；GUI 侧经 `EncryptCoreFacade.convertProgress` 转为 `OperationProgressDTO`，`FileProgress` 含 `state`（WAITING/RUNNING/FINISHED）、`progress`、`sourceFileSize`（人类可读字符串）等；`OperationResultDTO` 含 `successCount/failedCount` 与 `fileResults`（`targetFile`/`result`/`errorMsg`）。
- 完成提示音由 core 的 `FinishedListener` 播放（GUI 模式同样生效，EncryptDogFrame.finishOperation 注释已确认），无需重复实现。
- `ThemedConfirmDialog.show(owner, title, message, subMessage, confirmText, keepText)` 已有，阻塞式返回 boolean。
- `LogoUtil.loadImage(resource, size)` 等比缩放；`dock_logo.png` 为方形资源。
- 上次双进程托盘（2026-09-01-add-menu-bar-tray）已整体删除，端口 32345/32346 已释放。
- 实际构建与打包使用 JDK 21（`build-mac.sh` 依赖 jdk-21.0.1.jdk），pom source/target 15；macOS reopen/quit 回调走 JDK 9+ 标准 `java.awt.desktop` API（`Desktop.setQuitHandler` / `Desktop.addAppEventListener(AppReopenedListener)`），无模块封装问题。
- GUI 常驻成本：粒子/电路轨迹动画已退役；`ProgressPanel` 33ms 脉冲在 `removeNotify()` 显式停止；其余 Timer（拖拽涟漪/按钮 loading/占位提示）均为瞬态。空闲隐藏窗口无持续渲染。

## Goals / Non-Goals

**Goals:**

- 单进程零守护：托盘与 GUI 同一 JVM，无 IPC、无自愈、无守护进程
- 托盘是退出与唤回的枢纽：✕ 仅隐藏，Quit/Cmd+Q 是真退出（干活中需确认）
- 托盘不可用时整体优雅退化为托盘引入前行为（含 ✕=退出），不留"隐藏后无法退出"的死角

**Non-Goals:**

- 不做登录自启（launchd）
- 不隐藏 Dock 图标（保留双入口形态；纯托盘 `LSUIElement` 另议）
- 不做单色模板图标变体（彩色图标先做视觉验收，不合格再单独变更）
- 不动终端模式、密钥存储、打包脚本

## Decisions

### D1：单进程常驻，否决双进程与原生助手

上次双进程的唯一动机（粒子 16ms 动画常驻烧 CPU）已随动画退役消失；现存 Timer 全部瞬态，空闲隐藏窗口零渲染。双进程会带回守护进程、双端口、自愈、降级矩阵整套复杂度；Swift NSStatusItem 助手则引入第二工具链。单进程用 `java.awt.SystemTray` + `TrayIcon` 即可。

### D2：新增 `gui/interfaces/swing/tray/TrayManager`，生命周期钩子挂在 Frame 既有收尾点

`TrayManager` 进程内单例，职责：图标/菜单/tooltip、IDLE/WORKING 状态机、退出确认、Reveal、完成通知。接线沿用 Frame 现有三个钩子：

```
EncryptDogGui.launch (EDT)
  ├─ 1. 单例锁 acquireSingleton() → 失败即唤醒旧实例后 System.exit(0)
  ├─ 2. FlatLaf 主题安装(现有)
  ├─ 3. TrayManager.install(frame) → 成功才把 close 语义切为 HIDE_ON_CLOSE
  └─ 4. frame.setVisible(true) + 现有 Dock 图标

EncryptDogFrame
  ├─ startOperation → tray.onOperationStart()
  ├─ 进度 listener → tray.onProgress(dto)  (调度线程,内部切EDT)
  ├─ finishOperation → tray.onOperationFinished(result)
  └─ operationFailed → tray.onOperationFinished(null/异常文案)
```

备选：TrayManager 直接实现 `OperationListener` 与 GUI 表共享一个回调——否决，因为 listener 由 Frame 每次执行时新建，托盘需要的是"开始/结束"边界而不只是进度流，Frame 钩子语义最直白。

### D3：IDLE/WORKING 状态机，所有 AWT 变更统一 EDT 入口

状态为 `volatile` 枚举，`onProgress`（调度线程）先算好文案再 `SwingUtilities.invokeLater` 更新 tooltip 与状态行；tooltip/菜单更新按 **500ms 节流**（进度事件按块高频触发，避免 EDT 空转）。`onOperationStart/Finished` 与点击事件天然在 EDT 之外（worker/托盘事件线程），同样走 invokeLater。`install` 失败时 TrayManager 进入 disabled 态，所有钩子短路。

### D4：进度口径——字节加权整体进度

状态行 `Encrypting {done}/{total} · {pct}%`：

- `done` = `FileProgress.state == "FINISHED"` 的文件数（含失败文件——失败也已完成处理）
- `pct` = Σ(percentᵢ × sizeᵢ) / Σ(sizeᵢ)：percentᵢ 解析自 `progress` 字符串（`"41%"`），sizeᵢ 解析自 `sourceFileSize` 人类可读字符串（与 core `Utils` 格式化同源：B/KB/MB/GB）；解析失败的文件按文件数均摊权重
- 备选：纯文件数均摊（`(done + 当前文件%)/total`）——简单但大小文件混选时进度失真；改动 core 事件带字节数——侵入核心与终端 Dashboard 路径，收益不成比例，否决

### D5：关窗=隐藏，恢复路径三合一

`setDefaultCloseOperation(HIDE_ON_CLOSE)`（仅在托盘 install 成功后切换，见 D11）。窗口恢复统一走 `TrayManager.restoreWindow()`：EDT 上 `setVisible(true) + toFront + requestFocus`。三个触发来源：

1. 托盘 Show 菜单项
2. Dock 点击：`Desktop.addAppEventListener(AppReopenedListener)`（JDK 9+ 标准 API）；.app 形态双击时 macOS 本身不重复起进程、会派发 reopen 事件，同一回调覆盖
3. 单例锁收到 `show` 命令（D7）

### D6：退出语义——Cmd+Q 与托盘 Quit 共用 requestQuit()

`Desktop.setQuitHandler`（JDK 9+ 标准 API）拦截 Cmd+Q（不拦截则 Cmd+Q 直接杀进程，绕过干活中确认），与托盘 Quit 菜单项共用 `requestQuit()`：

- IDLE：直接 `System.exit(0)`（因 ✕ 已改 HIDE_ON_CLOSE，dispose 不再能退出，必须显式 exit）
- WORKING：`ThemedConfirmDialog.show(frame, "Quit EncryptDog", "Tasks are still running", "Files being processed may be left incomplete", "Quit anyway", "Keep working")`；确认 → `System.exit(0)`，取消/ESC → 继续
- 窗口隐藏时先 `restoreWindow()` 再弹浮层，保证浮层可见（spec 要求）
- worker 线程是 daemon，`System.exit` 直接终止一切，不等待

### D7：单例锁——localhost 端口 32346 + 轻量魔数握手

复用上次释放的端口号，仅 bind 127.0.0.1：

```
启动序(窗口创建前):
  try bind(32346) 成功 → 唯一实例; 起 accept 线程:
     收到 "ENCRYPTDOG1\nshow" → 回 "ok" → EDT restoreWindow
  bind 失败(AddressInUse) → connect(32346) → 发魔数+show:
     收到 "ok" → System.exit(0) (唤醒成功,自己退出)
     超时/连接拒绝/魔数不符 → 陌生进程占用 → 降级正常启动,记日志
```

- 魔数握手保留：一行成本，可区分"自己人"与陌生占用（D11 降级路径的判据）
- 备选：文件锁——锁文件残留需要清理策略，端口方案与上次实现经验一致，否决
- .app 形态天然单例（macOS 不重复起进程），锁主要保护 jar 形态；两个形态共用同一套逻辑无冲突

### D8：Reveal last output——最近成功输出文件

`TrayManager` 持有 `lastOutputFile`；`onOperationFinished(result)` 时从 `fileResults` 中取 `result == 成功` 且 `targetFile` 非空的**最后一个**条目，刷新菜单项 `setEnabled` 状态。点击时 `Desktop.browseFileDirectory(file)`（JDK9+，打开目录并选中文件，比 `Desktop.open(目录)` 体验好）；文件已不存在等异常静默 catch（spec 允许）。无成功输出时菜单项置灰。

### D9：完成通知——displayMessage，音效不重复播

`onOperationFinished`：状态回 IDLE；若 `frame.isVisible() == false`，`tray.displayMessage("EncryptDog", "3 succeeded · 1 failed")`（异常场景用异常文案）。完成提示音由 core `FinishedListener` 已播放（GUI 同样生效），托盘不重复播——避免双进程双播问题，单进程下也无需重复。

### D10：图标——logo.png 双档，彩色先行

`LogoUtil.loadImage("logo.png", 22/44)` → `BaseMultiResolutionImage` → `TrayIcon`，`setImageAutoSize(false)` 防缩放模糊。用户验收后确认使用横版品牌标志 `logo.png`（1536×1024，与标题栏同源），等比缩放后菜单栏内约为 22×14.6 扁带形态。彩色图标在 macOS 深浅色菜单栏的辨识度为视觉验收点；不达标时另出单色模板变体（独立变更，不在本次范围）。

### D11：降级矩阵——托盘失败时整体回退旧行为

| 情况 | 行为 |
| --- | --- |
| `SystemTray.isSupported()` false 或图标资源缺失 | `install` 返回 false：不切换 `HIDE_ON_CLOSE`（保持 ✕=退出），不挂图标、不注册 quit/reopen 回调，GUI 完全等同托盘引入前 |
| 单例端口被陌生进程占用（魔数不符） | 正常启动，记日志（可能短暂双图标，spec 允许） |
| reopen/quit 回调注册失败 | 托盘 Show 路径仍可用，Cmd+Q 退回系统默认直接退出 |

关键点：`HIDE_ON_CLOSE` 只有在托盘真正挂载成功后才启用，杜绝"窗口隐藏了却没有任何唤回/退出入口"的死角。

## Risks / Trade-offs

- [`.app` 形态下 `AppReOpenedListener`/quit handler 行为需真机验证] → 双形态（jar + .app/dmg）验收项；回调失效时托盘 Show 兜底
- [AWT `TrayIcon`/`displayMessage` 在 macOS 各版本的渲染与通知中心行为差异] → 真机视觉验收点（图标清晰度、深浅菜单栏辨识度、通知弹出）
- [隐藏窗口后台是否完全静止（零重绘零 Timer）] → 验收时用 Activity Monitor 观察 CPU；已知 Timer 均瞬态，若发现残留再收敛
- [进度百分比依赖字符串解析（`"41%"`、人类可读大小）] → 解析失败按文件数均摊兜底；格式与 core `Utils` 同源，变化概率低
- [干活中确认退出可能留下不完整输出文件] → 确认浮层文案明示，由用户决定；不做断点续传
- [单例锁端口与他程序冲突] → 魔数握手 + D11 降级矩阵，最坏情况等于托盘引入前的双开行为

## Migration Plan

- 无数据/持久化迁移，新 jar/.app 直接替换旧版即可获得托盘。
- 行为变化（✕ 不再退出）需在 README 中说明（作为任务项）。
- 回滚：撤销本变更即回到 ✕=退出、无托盘行为；无 schema、无数据耦合。
