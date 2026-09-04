## Why

目前获取新版本只能手动访问 GitHub Releases 页面自行下载 DMG 并替换，绝大多数用户不会主动检查，版本长期滞后。增加启动时自动检查与托盘手动检查入口，发现新版后弹窗确认下载，下载完成自动挂载 DMG 由用户拖拽安装，让更新成为自然动作，同时保留用户对安装动作的完全掌控。

## What Changes

- GUI 启动后（托盘就绪时）后台线程查询 GitHub `releases/latest` 一次；发现新版本时弹确认框询问是否下载
- 托盘空闲态与干活态菜单新增 "Check for updates" 手动检查项，手动检查结果始终即时反馈（有新版本弹窗、无新版本托盘通知"已是最新版本"）
- 用户确认后后台下载 DMG 到本地缓存目录，托盘菜单显示下载进度
- 下载完成自动挂载 DMG 并弹出 Finder 窗口，托盘通知用户拖入 Applications 完成安装；托盘保留 "Open downloaded update" 项可重新打开缓存 DMG
- 检查/下载失败、GitHub 限流、无新版：静默或轻提示，不影响任何既有功能
- 终端模式不参与更新检查
- 不做自替换安装（不修改运行中应用的 .app bundle、不自动退出重启）

## Capabilities

### New Capabilities

- `release-update-download`: 新版检查（启动自动 + 托盘手动）、弹窗确认、DMG 下载与进度、挂载打开与通知的完整行为

### Modified Capabilities

- `menu-bar-tray`: 托盘空闲/干活态菜单新增检查更新项与更新状态项（下载进度、"Open downloaded update"）

## Impact

- 新增 `UpdateChecker`（GUI 基础设施层）：GitHub API 查询、.dmg 资产挑选、版本比较、下载与进度回调
- `TrayManager`：新增更新状态字段与菜单项渲染（`applyIdle`/`applyWorking`）
- `EncryptDogGui.launch()`：托盘就绪后启动检查线程
- 复用现有 `ThemedConfirmDialog`（quit 确认同款）、`Desktop` API（browse 已有先例）、`dog.properties` 的 `project.version` 版本读取机制
- 无新增 Maven 依赖（JDK15 内置 `java.net.http.HttpClient`；无 JSON 库，tag_name 用正则提取）
- 网络侧依赖 GitHub API 未认证限流（60 次/小时/IP）：启动一次 + 手动触发，无压力
