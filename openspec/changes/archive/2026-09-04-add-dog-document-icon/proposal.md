## Why

.dog 文件关联已支持双击拉起 GUI 解密，但 Finder 中的 .dog 文件目前仍显示通用文档图标，品牌闭环缺最后一环。为文档类型声明品牌图标后，已安装应用的环境下所有 .dog 文件统一呈现 EncryptDog 品牌图标，与双击解密体验完整衔接。

## What Changes

- `build-mac.sh` 复用现有圆角预处理管线生成 `dog-document.icns`（与 Dock 图标同源），并拷贝进 `EncryptDog.app/Contents/Resources/`
- 打包后处理向 Info.plist 注入文档图标声明：`CFBundleDocumentTypes` 的 `CFBundleTypeIconFile` 与 `UTExportedTypeDeclarations` 的 `UTTypeIconFile`（jpackage 的文件关联 `icon` 属性仅 Linux 生效，macOS 必须后处理注入）
- build-mac.sh 追加图标声明断言（两键存在否则报错退出），沿用既有 PlistBuddy 断言模式
- README 增加文档图标说明与 Finder 图标缓存刷新提示
- 不改变任何加密格式与 Java 代码；图标声明随新 DMG 分发，旧版本不受影响

## Capabilities

### New Capabilities

（无。文档图标是既有 dog-file-association 能力下新增的关注点）

### Modified Capabilities

- `dog-file-association`: 新增「文档图标声明」需求（ADDED）——打包产物声明 .dog 文档类型品牌图标，已安装环境下 Finder 中所有 .dog 文件呈现该图标

## Impact

- `build-mac.sh`：新增 dog-document.icns 生成/拷贝与 PlistBuddy 注入、断言
- 产物层面：新 DMG 的 Info.plist 含文档图标键且 Resources 含 icns；旧版本与 jar 形态无图标声明
- README：图标说明与缓存刷新提示
- 无 Java 改动、无加密格式改动、无新增 Maven 依赖
- 图标解析由 LaunchServices 负责：安装新版后生效，个别情况需重启 Finder/注销（README 说明）
