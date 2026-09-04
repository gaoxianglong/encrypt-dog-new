## Context

GUI 基础设施现状：`TrayManager` 是静态类，`applyIdle()/applyWorking()` 每次状态切换整体重建菜单（menu.removeAll 后重加），干活/空闲态切换由 `onOperationStart/onOperationFinished` 驱动；`ThemedConfirmDialog.show(frame, title, line1, line2, okText, cancelText)` 已用于 quit 确认且含"窗口隐藏先恢复"先例；`Desktop.getDesktop()` 已有使用先例（browseFileDirectory）。运行时版本从 `dog.properties` 的 `project.version` 读取（`BannerInfo` 同机制）。pom 无 JSON 库（picocli/lombok/flatlaf 等），Java 15 起内置 `java.net.http.HttpClient`。GitHub API：未认证 60 次/小时/IP、请求必须带 User-Agent 头、`releases/latest` 的资产下载 URL 是重定向。

## Goals / Non-Goals

**Goals:**

- 启动检查一次 + 托盘手动检查，弹窗确认后后台下载并挂载打开 DMG
- 全链路失败静默/轻提示，绝不干扰加解密主流程

**Non-Goals:**

- 不做自替换安装（不碰运行中 .app、不自动退出重启、不写 /Applications）
- 不做终端模式检查、不做 sha256 校验发布、不做 notarization、不做更新缓存时间策略（每次启动查一次即可）
- 不引入 JSON 依赖

## Decisions

### 1. 新增 UpdateChecker 单类 + 显式状态机，TrayManager 只做展示

新增 `gui/infrastructure/update/UpdateChecker`（或 application/service），内部状态机 `IDLE → CHECKING → AVAILABLE / NO_UPDATE → DOWNLOADING → DOWNLOADED / FAILED`，单守护线程执行网络操作，状态变化经回调切 EDT 更新 TrayManager 与弹窗。TrayManager 只增加一个"更新项插槽"（applyIdle/applyWorking 共同渲染），不持有网络逻辑——保持展示层与网络层单向依赖，与现有 tray 只做视图的定位一致。

### 2. HTTP 与 JSON 处理

`HttpClient`（connect 3s / request 5s，`Redirect.NORMAL` 跟随资产下载重定向），请求头 `Accept: application/vnd.github+json`、`User-Agent: EncryptDog/<version>`（GitHub 强制要求）。无 JSON 库，用正则提取 `"tag_name"` 与资产段的 `"name"`/`"browser_download_url"`（按 `"assets"` 数组逐条扫描，取第一个 name 以 EncryptDog 开头且以 .dmg 结尾的条目）。备选：引入 jackson/gson——否决：为一个字段增加依赖与构建体积不值；解析失败走"无新版本"静默路径，最坏结果是少提示一次更新而非崩溃。

### 3. 版本比较

归一化（剥 v 前缀、剥 -RELEASE 后缀）后逐段数值比较（补零对齐，如 2.1 与 2.1.0 相等），任一段不可解析视为无新版本。备选：`Runtime.Version.parse().compareTo()`——否决：实施中发现当前运行 JDK 对 `2.1.0` 形态的版本串解析直接抛 `IllegalArgumentException`（"Invalid version string"），行为不可依赖；手写逐段比较对该 tag 格式（纯点分数字）完全够用且零依赖。归一化与比较逻辑独立成纯函数（`VersionComparator`），单测覆盖 v2.1.0/2.1.0-RELEASE/相同版本/解析失败/双位版本号五类输入。

### 4. 缓存目录与挂载

缓存目录 `~/Library/Caches/EncryptDog/update/`（macOS 专属功能，`user.home` 下拼路径，不存在则 mkdirs，失败静默），文件名 `EncryptDog-{version}.dmg`，同版本覆盖写。下载完成用 `Desktop.open(dmgFile)` 挂载并弹 Finder（等同用户双击 DMG，尊重系统挂载偏好）；抛异常时备选 `ProcessBuilder("hdiutil", "attach", dmg)` 挂载后再 `Desktop.open` 挂载点。DMG 保留在缓存目录供 "Open downloaded update" 重开；不主动删除（下次同版本下载覆盖）。备选：下到 Downloads——否决：污染用户目录且难以定位；下到临时目录——否决：重启/清理后"重开"能力丢失。

### 5. 托盘更新项插槽

TrayManager 增加静态字段 `updateState/updateVersion/updatePct` 与共享渲染辅助：插槽位于 Reveal last output（或状态行）之后、Quit 之前，恰渲染一项——IDLE 态 "Check for updates"、检查中禁用 "Checking for updates..."、AVALABLE "Update available {version}"、下载中禁用 "Downloading {version} · {pct}%"、DOWNLOADED "Open downloaded update"。点击语义：Check for updates → 触发检查（检查/下载中忽略）；Update available → 重弹确认框；Open downloaded update → Desktop.open 缓存 DMG。通知：手动检查失败 "Update check failed"、下载失败 "Update download failed"、下载完成 "v{version} downloaded. Drag EncryptDog into Applications to update."——走既有 `trayIcon.displayMessage`，语言与既有菜单一致（英文、无 emoji）。手动检查无新版本不走通知而走弹窗（用户确认的交互：复用 `ThemedConfirmDialog` 单按钮模式 `showMessage`，message "Already up to date"，仅一个 OK 按钮居中，无副文案）。自动检查失败与无新版本均保持静默，不产生任何提示。

### 6. 弹窗与干活态协调

确认框复用 `ThemedConfirmDialog.show(frame, "EncryptDog Update", "New version {v} is available", "Download now, or check later from the menu bar", "Download", "Later")`。启动检查（自动）出结果时若 `TrayManager` 处于 WORKING 态（新增包内静态 `isBusy()` 访问器），结果挂起，`onOperationFinished` 回空闲后经回调补弹；手动检查用户在场，立即弹。窗口隐藏时先 `TrayManager.restoreWindow()` 再弹（quit 确认同款模式）。frame 引用由 `EncryptDogGui.launch` 在 EDT 建窗后传给 UpdateChecker（托盘降级场景 frame 仍存在，弹窗可用）。

### 7. 启动接入点

`EncryptDogGui.launch()` 在 `TrayManager.install(frame)` 返回后（无论成败）以守护线程启动一次检查——托盘降级时弹窗与下载仍可用，仅菜单/通知缺失（与 spec 一致）。终端模式经 `Starter` 走 picocli，完全不触及更新代码。

## Risks / Trade-offs

- [GitHub 未认证限流 60/h（共享出口 IP 可能更早触发 403）] → 403 与网络错误同路径静默；每次启动仅 1 次 + 手动，正常使用远低于阈值
- [releases JSON 结构调整导致正则失效] → 静默降级为无新版本，不影响功能；GitHub API 格式长期稳定，风险低
- [Desktop.open 挂载失败（异常环境）] → hdiutil attach 备选路径，再失败静默，托盘回到可检查状态
- [DMG 体积大、慢网下载耗时长] → 后台线程 + 托盘进度，不阻塞 UI；不做取消/断点续传（接受范围外，下载失败即回可检查态）
- [启动检查弹窗与 quit 确认竞态（同用确认框组件）] → 弹窗均由 EDT 串行触发，状态机单线程推进，无并发弹窗
- [jar 形态 GUI 用户也会收到更新提示] → 有意为之：README 本就推荐 DMG 形态，jar 用户升级路径同样是下载新 DMG

## Migration Plan

纯运行时功能，无数据迁移。验证方式：本地起一个 mock HTTP 服务（或临时构造假 JSON 指向测试 asset）验证弹窗/下载/挂载全链路；用现有 2.0.5 release 验证"无新版本"静默路径。回滚：删除该功能仅需还原相关类与 TrayManager 菜单渲染，无持久化副作用。
