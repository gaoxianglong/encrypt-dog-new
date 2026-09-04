## Why

目前解密一个 .dog 文件必须先手动启动 EncryptDog（GUI 或终端）再自行选择文件，操作路径长。macOS 用户在 Finder 中双击 .dog 时系统找不到关联应用，加密文件显得"不完整"。声明文件关联并接收系统打开事件后，双击 .dog 即可直接拉起 GUI 并预填解密，产品体验与原生 macOS 应用对齐。

## What Changes

- `build-mac.sh` 的 jpackage 增加 `--file-associations`，声明 `.dog` 扩展名（生成 Info.plist 的 CFBundleDocumentTypes），新增关联声明配置文件
- GUI 启动路径注册 `Desktop.setOpenFileHandler`，接收 macOS 的 odoc 打开事件（含冷启动双击与运行中双击两种来源）
- 双击一个或多个 .dog：GUI 以解密模式预填文件列表；窗口隐藏（托盘态）时一并唤回
- 不改变终端模式行为；无 odoc 事件时 `--gui` 参数预填路径与现在完全一致

## Capabilities

### New Capabilities

- `dog-file-association`: `.dog` 文件关联声明，以及双击/打开 .dog 时 GUI 解密预填与窗口唤回行为

### Modified Capabilities

（无。swing-gui 的 `--gui` 参数路由、菜单栏托盘、加解密核心均不改变可观察行为）

## Impact

- `build-mac.sh`：jpackage 增加 `--file-associations` 参数
- 新增 `file-associations.properties`（仓库根目录，打包时被 jpackage 消费）
- `EncryptDogGui.launch()`：注册 OpenFilesHandler，odoc 文件并入预填 DTO（复用现有 `EncryptFormDTO`/`frame.prefill` 链路）
- 产物层面：新 DMG 的 Info.plist 含 CFBundleDocumentTypes；旧版本与 jar 形态不受影响
- 无新增 Maven 依赖（使用 JDK 内置 `java.awt.desktop` API）
- 关联注册属于 LaunchServices 行为，首次安装后可能需要运行一次应用或手动"打开方式"一次，README 需补充说明
