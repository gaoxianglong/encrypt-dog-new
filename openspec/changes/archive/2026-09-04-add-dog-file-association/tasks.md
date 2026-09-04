## 1. 打包关联声明

- [x] 1.1 新增仓库根目录 `file-associations.properties`（extension=dog、mime-type=application/x-encryptdog、description），确认文件内容与设计一致
- [x] 1.2 `build-mac.sh` 的 jpackage 调用增加 `--file-associations file-associations.properties`，执行 `./build-mac.sh` 后用 PlistBuddy 读取 `dist/EncryptDog.app/Contents/Info.plist` 的 CFBundleDocumentTypes，确认含 dog 扩展名条目
- [x] 1.3 在 build-mac.sh 追加 Info.plist 校验断言（CFBundleDocumentTypes 含 dog 时继续、否则报错退出），重跑 `./build-mac.sh` 验证断言通过

## 2. GUI 打开事件接入

- [x] 2.1 `EncryptDogGui.launch()` 在单例锁之后、EDT 建窗之前同步注册 `Desktop.setOpenFileHandler`：回调把 `OpenFilesEvent.getFiles()` 追加进 volatile pending 列表后 invokeLater 触发合并；捕获 Throwable（二次注册/平台不支持）静默降级。以 `mvn -q compile` 编译通过为验证
- [x] 2.2 实现 pending 合并逻辑：frame 为 null 直接返回；存在 pending 文件时构造 `EncryptFormDTO(isEncrypt=false, sourceFilePaths=pending)` 走 `frame.prefill()` 并清空 pending；建窗 invokeLater 尾部同样调用合并。以无 odoc 事件时 `--gui` 预填行为不变为验证
- [x] 2.3 合并完成后调用 `TrayManager.restoreWindow()` 唤回窗口（隐藏态恢复、可见态聚焦）。以代码走查确认与单例唤醒共用同一恢复路径为验证
- [x] 2.4 干活态 odoc 事件处理：忙碌时清空 pending、唤回窗口并弹 `ThemedConfirmDialog` 提示（复用 TrayManager 忙碌状态访问器，与 add-release-update-download 变更的 isBusy() 同一实现）；以任务执行中双击 .dog 手动验证弹窗提示、文件未预填、进行中任务正常完成

## 3. 文档与验收

- [x] 3.1 README 增加"双击 .dog 解密"说明与首次关联注意事项（安装后先运行一次 app，或右键"打开方式"选择 EncryptDog 一次），确认描述与 spec 一致
- [x] 3.2 构建 DMG 后手动验收：冷启动双击单个/多个 .dog 均以 Decrypt 模式预填；窗口隐藏时双击唤回并预填；纯 `--gui` 启动与终端模式行为不变
- [x] 3.3 执行 `openspec validate add-dog-file-association --strict` 通过，且 `mvn -q package -DskipTests` 构建产物正常
