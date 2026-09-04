## 1. 版本判定与查询基础件

- [x] 1.1 新增版本归一化与比较纯函数（剥 v 前缀与 -RELEASE 后缀后 `Runtime.Version.parse().compareTo`，解析失败视为无新版本），当前版本从 dog.properties 的 project.version 读取作为比较基准；编写单元测试覆盖 v2.1.0 高于 2.0.5、相同版本、2.1.0-RELEASE、解析失败四类输入，`mvn -q test -Dtest=...` 通过
- [x] 1.2 新增 `UpdateChecker` 骨架：HttpClient 请求 `https://api.github.com/repos/gaoxianglong/encrypt-dog-new/releases/latest`（User-Agent/Accept 头、connect 3s/read 5s、Redirect.NORMAL），正则提取 tag_name 与 .dmg 资产（name 以 EncryptDog 开头且以 .dmg 结尾）的 browser_download_url，以本地 mock HTTP 服务返回样例 JSON 验证解析正确、404/403/网络异常均静默返回无新版本

## 2. 下载与挂载

- [x] 2.1 实现 DMG 下载：目标目录 `~/Library/Caches/EncryptDog/update/`（不存在则创建，失败静默），文件名 `EncryptDog-{version}.dmg`，下载进度按 Content-Length 计算并通过回调上报（节流约 500ms），以 mock 服务提供可下载文件验证进度回调与落盘完整
- [x] 2.2 实现下载完成挂载打开：优先 `Desktop.open(dmgFile)`，失败备选 `hdiutil attach` 后 `Desktop.open` 挂载点，再失败静默回可检查态；以本地构造的 dmg 文件验证挂载与 Finder 打开（或验证失败路径静默降级）
- [x] 2.3 缓存复用：再次下载同版本覆盖旧文件；"Open downloaded update" 直接 `Desktop.open` 缓存 DMG，以手动二次点击验证重开行为

## 3. 托盘菜单与弹窗

- [x] 3.1 `TrayManager` 增加更新项插槽（updateState/updateVersion/updatePct 静态字段 + 共享渲染辅助）：applyIdle 与 applyWorking 在 Reveal last output/状态行之后、Quit 之前渲染单一项，覆盖 Check for updates / Checking...（禁用）/ Update available {version} / Downloading {version} · {pct}%（禁用）/ Open downloaded update 五种状态；`mvn -q compile` 通过且代码走查确认五态齐全、文案全英文无 emoji
- [x] 3.2 增加 `TrayManager.isBusy()` 包内访问器暴露干活态，供更新弹窗延迟判断；以代码走查确认不影响既有状态机
- [x] 3.3 `UpdateChecker` 接入弹窗：发现新版本回调 EDT 上弹 `ThemedConfirmDialog`（窗口隐藏先 restoreWindow；isBusy 时挂起至 onOperationFinished 回空闲后补弹），Download 进入下载态、Later 回到 Update available 项；托盘手动检查结果无新版本时弹窗提示 "Already up to date"（窗口隐藏先恢复可见）。以本地 mock 服务切换"有新/无新/接口失败"三种响应手动验证弹窗与通知（接口失败时通知检查失败）

## 4. 启动接入与状态守卫

- [x] 4.1 `EncryptDogGui.launch()` 在 `TrayManager.install(frame)` 返回后（无论成败）以守护线程启动一次检查，frame 引用传入 UpdateChecker；终端模式确认零改动（走查 `Starter` 不触达更新代码）
- [x] 4.2 状态机守卫：检查中/下载中重复触发 Check for updates 被忽略；下载失败弹通知 "Update download failed" 并回到可检查态；下载完成弹通知 "v{version} downloaded..."，以 mock 服务注入失败/成功/重复点击三场景验证
- [x] 4.3 验收全链路：本地 mock 服务模拟高版本 release + 可下载 dmg，验证启动弹窗 → 确认下载 → 托盘进度 → 挂载打开 → 通知完整流程；再以真实 GitHub API 验证无新版本静默与限流降级；补充验证干活态下自动检查出结果延迟到任务结束补弹，以及托盘降级场景（SystemTray 不可用）下弹窗与下载仍可用
- [x] 4.4 执行 `openspec validate add-release-update-download --strict` 通过，且 `mvn -q package -DskipTests` 构建产物正常
