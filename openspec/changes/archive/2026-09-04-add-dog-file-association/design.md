## Context

打包链路：`build-mac.sh` 用 jpackage（JDK21）产出 EncryptDog.app 与 DMG，`--arguments "--gui"` 使 app 启动时走 `Starter.parseGuiArgs` → `EncryptDogGui.launch(prefill)` → `frame.prefill()` 的预填链路（`EncryptFormDTO` 含 isEncrypt/sourceFilePaths 等字段，`EncryptFormPanel.prefill` 已支持模式切换）。macOS 双击文档不会把文件放进 argv，而是发 odoc AppleEvent；Java 侧唯一入口是 `java.awt.desktop` 的 `Desktop.setOpenFileHandler`。单例锁（32346 端口）只对二次命令行启动生效——LaunchServices 对已运行应用直接投递 odoc，不产生新进程。

## Goals / Non-Goals

**Goals:**

- 冷启动双击与运行中双击两条路径都以解密模式预填并正确唤回窗口
- 打包产物 Info.plist 自动含 CFBundleDocumentTypes，不手工维护 plist

**Non-Goals:**

- 不做 Finder 右键菜单/Quick Action（纯 Java 成本高收益低）
- 不自动填入密钥、不自动开始执行（密钥始终用户输入）
- 不做终端模式的文件关联、不做 .dog 文档图标、不做 URI scheme

## Decisions

### 1. 关联声明用 jpackage `--file-associations`，不手工改 Info.plist

新增仓库根目录 `file-associations.properties`（extension=dog、mime-type=application/x-encryptdog、description），`build-mac.sh` 的 jpackage 调用加 `--file-associations file-associations.properties`。jpackage 自动生成 CFBundleDocumentTypes，每次重建再生，不随 jpackage 版本手工漂移。

备选：jpackage 产出后用 PlistBuddy 注入。否决：多一段维护逻辑，且 plist 与 bundle 生命周期解耦容易出错。可在 build-mac.sh 追加 PlistBuddy 断言（读 CFBundleDocumentTypes 校验 dog 存在）作为防回归检查，但声明本体以 jpackage 为准。

### 2. OpenFilesHandler 在 launch() 同步段注册，早于 EDT 建窗

`EncryptDogGui.launch()` 中 `SingleInstanceGuard.ensureSingle()` 之后、`SwingUtilities.invokeLater` 之前同步注册。理由：冷启动时 odoc 事件在 JVM 启动瞬间到达，AWT 原生层把事件排队到 handler 注册时再投递，注册越早丢事件风险越小；回调在 EDT 上执行，因此注册本身放主线程（非 EDT）安全。注册必须加守卫：`setOpenFileHandler` 只允许调用一次（二次抛 IllegalArgumentException）、非 macOS 抛 UnsupportedOperationException——沿用项目惯例 catch Throwable 静默降级（jar/终端路径不受影响）。

### 3. 事件与建窗的时序解耦：pending 列表 + EDT 上统一合并

回调（EDT）与建窗代码（EDT，invokeLater）之间存在"事件先到、frame 未建"和"frame 先建、事件后到"两种时序。统一方案：handler 仅把 `OpenFilesEvent.getFiles()` 追加进 volatile pending 列表，再 invokeLater 触发同一个 `mergePending()`；建窗 invokeLater 尾部也调用 `mergePending()`。merge 在 EDT 上执行：frame 为 null 则直接返回（等建窗路径合并），否则构造预填 DTO 应用。

### 4. 合并语义：odoc 文件存在即以解密模式整体预填

存在 pending 文件时：`isEncrypt=false`、`sourceFilePaths` 置为全部 pending 文件、清空 pending；不存在的启动路径原样走 `--gui` 参数预填。选择"整体替换"而非"追加到参数预填"：双击 .dog 是明确的解密意图，与命令行预填叠加语义模糊；且 jpackage 冷启动 argv 恒为 `--gui`，叠加场景实际不存在。空闲态运行中双击时同理直接切换表单为解密模式（覆盖用户当前未提交的表单编辑，可接受）。例外——任务执行中（干活态）收到 odoc：清空 pending、`restoreWindow()` 唤回窗口、弹 `ThemedConfirmDialog` 纯提示（title "Task in progress"、message 说明文件未加载，confirmText "OK"、keepText "Close"，两按钮均仅关闭），不预填、不触碰执行中的表单。干活态判断复用 `TrayManager` 忙碌状态访问器——与 add-release-update-download 变更计划中的 isBusy() 为同一实现，两变更实施时合并去重，避免重复修改 TrayManager。

### 5. 运行中双击复用 TrayManager.restoreWindow()

odoc 回调合并完成后调用 `TrayManager.restoreWindow()`（其内部已处理 installed==false 与 EDT 切换），隐藏态唤回、可见态前台聚焦，不新开窗口——与单例唤醒、Dock 点击共用同一条恢复路径。

## Risks / Trade-offs

- [LaunchServices 关联注册延迟：新装 app 双击 .dog 可能提示选择应用] → README 说明：首次安装后先运行一次 app 或右键"打开方式"选择 EncryptDog 一次即可；关联声明随 DMG 分发，旧版本无此能力
- [jpackage 生成的 CFBundleTypeRole 默认非 Viewer] → 仅影响系统展示的文档角色标注，不影响双击启动与 odoc 投递，接受默认值
- [冷启动事件丢失（若注册过晚或平台投递异常）] → 注册置于 launch 同步段第一优先；实测验证项覆盖冷启动路径
- [运行中双击覆盖用户未提交的表单编辑] → 可接受的 UX 取舍，双击本身即是明确的新意图
- [干活态双击仅提示不预填，用户需任务结束后再次打开文件] → 与用户确认的交互取舍：提示框明确告知，避免执行期间表单状态歧义
- [app 未签名/未 notarize 时关联注册可能不生效] → 与现状一致（用户已通过首次右键打开建立信任），README 不新增承诺

## Migration Plan

无运行时数据迁移。产物侧：新版本 DMG 才携带关联声明，构建后需重新打包并让用户重新安装。回滚：恢复旧 DMG 即完全回到变更前行为（Java 侧处理注册失败静默，不影响旧产物）。
