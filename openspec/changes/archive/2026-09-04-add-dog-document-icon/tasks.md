## 1. 打包图标声明

- [x] 1.1 `build-mac.sh` 在 jpackage 之后拷贝图标到 bundle：`cp /tmp/EncryptDog.icns "$DIST/EncryptDog.app/Contents/Resources/dog-document.icns"`，执行 `./build-mac.sh` 后以 `ls` 确认 icns 文件在位
- [x] 1.2 `build-mac.sh` 用 PlistBuddy 向 Info.plist 注入 `CFBundleDocumentTypes:0:CFBundleTypeIconFile` 与 `UTExportedTypeDeclarations:0:UTTypeIconFile`（值 dog-document），执行构建后以 PlistBuddy Print 确认两键存在
- [x] 1.3 `build-mac.sh` 追加图标声明断言（两键与 icns 文件均存在才继续，否则报错退出，失败信息与关联声明断言区分），重跑 `./build-mac.sh` 验证断言通过

## 2. 文档与验收

- [x] 2.1 README 增加"文档图标"说明与 Finder 图标缓存刷新提示（如不生效重启 Finder 或注销一次），确认描述与 spec 一致
- [x] 2.2 构建 DMG 并安装后手动验收：Finder 中 .dog 文件显示与 Dock 图标同源的品牌图标；图标缓存未刷新时按 README 操作后生效；加解密功能与 .dog 文件内容不受影响
- [x] 2.3 执行 `openspec validate add-dog-document-icon --strict` 通过，且 `mvn -q package -DskipTests` 构建产物正常
