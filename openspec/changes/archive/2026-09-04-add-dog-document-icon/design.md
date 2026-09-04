## Context

打包链路现状：`build-mac.sh` 已有 PIL 圆角预处理（82% 内框 + 22% 半径）与 iconutil 十档生成管线（产出 `/tmp/EncryptDog.icns` 供 jpackage `--icon` 使用）；上一变更（add-dog-file-association）已通过 jpackage `--file-associations` 在 Info.plist 生成了 CFBundleDocumentTypes 与 UTExportedTypeDeclarations（UTI `com.gxl.encryptdog.dog`、LSHandlerRank=Owner），并追加了 PlistBuddy 断言模式（关联声明校验）。jpackage 的文件关联属性 `icon=` 仅对 Linux 的 .desktop 生效，macOS 不写入 CFBundleTypeIconFile——文档图标必须打包后处理注入。

## Goals / Non-Goals

**Goals:**

- 已安装新版的环境下，Finder 中所有 .dog 文件显示与 Dock 图标同源的品牌图标
- 纯打包层改动（build-mac.sh + Info.plist 注入 + README），零 Java 改动、零格式改动

**Non-Goals:**

- 不做文档样式图标变体（用户明确指定使用该 logo，与 Dock 图标同款）
- 不做逐文件写入图标（ResourceFork 老方案：拷贝易丢、跨系统失效）
- 不处理未安装 EncryptDog 的机器上的图标（图标随 app 分发，属预期）

## Decisions

### 1. 图标同源复用：dog-document.icns 与 app 图标同一管线产出

build-mac.sh 在现有 iconutil 产出的 `/tmp/EncryptDog.icns` 基础上，直接以 `dog-document.icns` 为名拷入 bundle Resources（同一文件、同源同比例，与 Dock 图标观感一致）。

备选：PIL 生成"白纸叠 logo"文档样式变体。否决：用户指定使用该 logo 图标，同款即所求；变体留作后续可选优化。

### 2. 注入方式：PlistBuddy 后处理，而非 jpackage 参数

jpackage `--file-associations` 的 `icon=` 属性仅 Linux 生效（jpackage 已知限制），macOS 产物不会写入 CFBundleTypeIconFile。因此 jpackage 完成后：

1. `cp /tmp/EncryptDog.icns "$DIST/$APP_NAME.app/Contents/Resources/dog-document.icns"`
2. `PlistBuddy -c "Add :CFBundleDocumentTypes:0:CFBundleTypeIconFile string dog-document"`（键值为不含扩展名的文件名，macOS 约定）
3. `PlistBuddy -c "Add :UTExportedTypeDeclarations:0:UTTypeIconFile string dog-document"`
4. 断言两键存在且 icns 文件在位，缺失即 `exit 1`

备选：jpackage `--resource-dir` 拷贝资源。否决：行为依赖 jpackage 版本对 resource-dir 在 macOS 上的落点约定，显式 cp + PlistBuddy 确定性强，且与既有断言模式一致。

### 3. 断言扩展与顺序

在既有关联声明断言之后追加图标断言（先验 CFBundleDocumentTypes 存在、再验图标键），失败信息区分"关联声明缺失"与"图标声明缺失"，便于回归定位。

### 4. Finder 图标缓存

LaunchServices 解析文档图标有缓存：安装新版后个别情况需重启 Finder（`killall Finder`）或注销一次才刷新。README 写一句提示即可，不做自动刷新（侵入系统行为，收益低）。

## Risks / Trade-offs

- [Finder 图标缓存延迟：装完仍显示通用图标] → README 提示重启 Finder/注销；LS 通常会自动刷新
- [图标跟随默认处理器解析：用户改默认程序后图标可能变化] → 当前 LSHandlerRank=Owner 为默认处理器，正常场景不受影响
- [app 被移动/删除后图标退化] → 预期行为，重新注册即恢复
- [重复执行 build-mac.sh 时 PlistBuddy Add 键已存在报错] → 脚本每次 jpackage 全新产 app-image，无累积问题；断言用 Print 读键而非依赖 Add 幂等
- [与 dog-file-association 既有断言耦合] → 顺序执行、失败信息区分，回归时互不遮蔽

## Migration Plan

无运行时数据迁移。产物侧：新版本 DMG 才携带图标声明，用户需重装；回滚：恢复旧 DMG 即回到无图标状态（图标随 app 分发，不残留系统级配置）。
