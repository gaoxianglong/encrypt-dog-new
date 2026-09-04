# dog-file-association Specification

## Purpose

让 .dog 加密文件成为 macOS 可双击打开的文档类型：打包产物声明文件关联，双击或"打开方式"选择 EncryptDog 时直接拉起 GUI 并以解密模式预填文件列表。

## Requirements

### Requirement: 打包产物声明 .dog 文件关联

macOS 打包产物 EncryptDog.app 的 Info.plist SHALL 声明 CFBundleDocumentTypes 包含扩展名 dog（含对应的文档类型名称），使 LaunchServices 能够将 .dog 文件与 EncryptDog 关联。jar 形态与终端模式 SHALL NOT 声明任何文件关联。

#### Scenario: 新 DMG 产物含关联声明

- **WHEN** 执行 macOS 打包脚本构建 EncryptDog.app
- **THEN** 产物 Info.plist 中存在包含 dog 扩展名的 CFBundleDocumentTypes 条目

#### Scenario: jar 形态无关联

- **WHEN** 用户以 jar 形态运行加密狗
- **THEN** 系统不注册任何文件关联，不改变系统已有关联设置

### Requirement: 双击 .dog 冷启动预填解密

应用未运行状态下双击（或以"打开方式"选择）一个或多个 .dog 文件时，系统 SHALL 启动 GUI 并 SHALL 以解密模式预填表单：模式为 Decrypt、文件列表为本次打开的全部 .dog 文件；密钥 SHALL 仍由用户在界面输入，系统 SHALL NOT 自动填入密钥或自动开始执行。

#### Scenario: 双击单个 .dog 冷启动

- **WHEN** 应用未运行，用户在 Finder 双击一个 .dog 文件
- **THEN** EncryptDog GUI 启动，模式为 Decrypt，文件列表包含该文件，未自动开始执行

#### Scenario: 一次打开多个 .dog

- **WHEN** 应用未运行，用户选中多个 .dog 文件以打开方式交给 EncryptDog
- **THEN** GUI 以 Decrypt 模式启动，文件列表包含全部被打开的文件

### Requirement: 运行中双击 .dog 预填并唤回窗口

应用已运行（窗口可见或隐藏）且空闲时，用户双击 .dog 文件 SHALL NOT 启动第二个进程或第二个窗口；现有实例 SHALL 以解密模式预填该文件并 SHALL 将窗口带回前台。任务执行中（干活态）收到打开事件时，系统 SHALL NOT 预填表单，SHALL 将窗口带回前台并弹窗提示存在进行中的任务，进行中的任务 SHALL NOT 受影响。

#### Scenario: 窗口可见时双击 .dog

- **WHEN** 应用已运行且窗口可见，用户双击一个 .dog 文件
- **THEN** 不出现新进程/新窗口，现有窗口切换到 Decrypt 模式并预填该文件

#### Scenario: 窗口隐藏时双击 .dog

- **WHEN** 应用以托盘态运行（窗口隐藏），用户双击一个 .dog 文件
- **THEN** 现有窗口被带回前台，切换到 Decrypt 模式并预填该文件

#### Scenario: 干活态双击弹窗提示

- **WHEN** 任务执行中（干活态），用户双击一个 .dog 文件
- **THEN** 不出现新进程/新窗口，现有窗口被带回前台并弹出提示框告知存在进行中的任务，文件不被预填，进行中的任务不受影响

### Requirement: 无打开事件的启动行为冻结

未伴随 .dog 打开事件的 GUI 启动（含 `--gui` 参数预填路径）SHALL 保持现有行为完全一致；系统 SHALL NOT 因注册打开事件处理而改变终端模式、jar 形态或任何其它启动路径的可观察行为。

#### Scenario: 纯 --gui 启动不受影响

- **WHEN** 用户以 `java -jar encryptdog.jar --gui [-e -s ...]` 方式启动且无文件打开事件
- **THEN** 表单预填与模式行为与本次变更前完全一致

#### Scenario: 终端模式不受影响

- **WHEN** 用户以终端模式（不带 --gui）运行
- **THEN** 行为与本次变更前完全一致，不注册文件打开处理

#### Scenario: 不支持打开事件的平台静默降级

- **WHEN** 运行平台不支持文件打开事件（如无桌面 API 支持的非 macOS 环境）
- **THEN** 系统静默跳过打开事件处理，GUI 启动与使用不受影响

### Requirement: 文档图标声明

macOS 打包产物 EncryptDog.app SHALL 为 .dog 文档类型声明品牌图标：Info.plist 的文档类型声明 SHALL 包含图标键（CFBundleTypeIconFile 与 UTTypeIconFile）且指向包内 Resources 下实际存在的 icns 文件，图标 SHALL 与 Dock 图标同源。已安装该应用的设备上，Finder 中所有 .dog 文件 SHALL 呈现该品牌图标。jar 形态与终端模式 SHALL NOT 声明任何文档图标。文档图标声明 SHALL NOT 改变 .dog 文件格式与任何加密行为。

#### Scenario: 新 DMG 产物含图标声明

- **WHEN** 执行 macOS 打包脚本构建 EncryptDog.app
- **THEN** 产物 Info.plist 的 CFBundleDocumentTypes 与 UTExportedTypeDeclarations 均包含指向 dog-document 的图标键，且 Contents/Resources 下存在对应 icns 文件

#### Scenario: 已安装环境下 Finder 展示品牌图标

- **WHEN** 用户安装新版本后查看 .dog 文件
- **THEN** Finder 中 .dog 文件显示 EncryptDog 品牌图标（与 Dock 图标同源）

#### Scenario: jar 形态无图标声明

- **WHEN** 用户以 jar 形态运行加密狗
- **THEN** 系统不声明任何文档图标，不改变系统已有文件类型图标

#### Scenario: 图标声明不改变文件格式

- **WHEN** 声明文档图标后对文件执行加解密
- **THEN** .dog 文件格式与内容与变更前完全一致，加解密行为不变
