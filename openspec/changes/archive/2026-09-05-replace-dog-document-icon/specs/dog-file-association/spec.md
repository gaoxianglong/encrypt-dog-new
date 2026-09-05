# dog-file-association Spec Delta

## MODIFIED Requirements

### Requirement: 文档图标声明

macOS 打包产物 EncryptDog.app SHALL 为 .dog 文档类型声明品牌图标：Info.plist 的文档类型声明 SHALL 包含图标键（CFBundleTypeIconFile 与 UTTypeIconFile）且指向包内 Resources 下实际存在的 icns 文件，图标 SHALL 为独立品牌文档图标（由 dog-document.png 生成、竖版品牌图形，与 Dock 应用图标不同源）。已安装该应用的设备上，Finder 中所有 .dog 文件 SHALL 呈现该文档图标。jar 形态与终端模式 SHALL NOT 声明任何文档图标。文档图标声明 SHALL NOT 改变 .dog 文件格式与任何加密行为。

#### Scenario: 新 DMG 产物含图标声明

- **WHEN** 执行 macOS 打包脚本构建 EncryptDog.app
- **THEN** 产物 Info.plist 的 CFBundleDocumentTypes 与 UTExportedTypeDeclarations 均包含指向 dog-document 的图标键，且 Contents/Resources 下存在对应 icns 文件（独立于应用 icns 生成）

#### Scenario: 已安装环境下 Finder 展示品牌图标

- **WHEN** 用户安装新版本后查看 .dog 文件
- **THEN** Finder 中 .dog 文件显示独立品牌文档图标（竖版品牌图形，与应用 Dock 图标不同源）

#### Scenario: jar 形态无图标声明

- **WHEN** 用户以 jar 形态运行加密狗
- **THEN** 系统不声明任何文档图标，不改变系统已有文件类型图标

#### Scenario: 图标声明不改变文件格式

- **WHEN** 声明文档图标后对文件执行加解密
- **THEN** .dog 文件格式与内容与变更前完全一致，加解密行为不变
