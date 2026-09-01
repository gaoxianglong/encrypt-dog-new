# Rename to EncryptDog

## Why

品牌名在代码库中四处不一致:窗口标题栏显示 "EncryptionDog",而 macOS 打包产物(.app/.dmg)已是 "EncryptDog",CLI 命令名为 `encrypt-dog`,jar 产物名是 `dog-2.0.4.jar`。用户已拍板统一为 **EncryptDog**(构词上精确对应 看门狗→watchdog 的模板,即"会加密的狗"),本变更将剩余未统一的表面全部收敛到这个名称。

## What Changes

- 窗口标题栏品牌名:`UiConstants.APP_NAME` 由 "EncryptionDog" 改为 "EncryptDog"
- CLI 命令名:picocli `@Command(name = "encrypt-dog")` 改为 `encryptdog`(usage 输出随之变化)——**BREAKING**(依赖命令名做脚本匹配的用户需更新)
- Maven 产物:`pom.xml` 中 `finalName` 由 `dog-${project.version}` 改为 `encryptdog-${project.version}`,artifactId 由 `encrypt-dog-new` 改为 `encryptdog`——jar 名变为 `encryptdog-2.0.4.jar`,**BREAKING**(下载链接、`dog` 别名内路径、构建脚本引用均需更新;别名名 `dog` 本身不变)
- `build-mac.sh`:jar 复制路径 `target/dog-${VERSION}.jar` 改为 `target/encryptdog-${VERSION}.jar`(.app/.dmg 名不变,脚本中 `APP_NAME` 已是 "EncryptDog")
- README:品牌引用 "EncryptionDog" 改为 "EncryptDog",`dog-2.0.4.jar` 引用与 usage 示例更新为新名
- 内部标识字符串顺带统一:`Starter` 临时文件前缀 "encrypt-dog-dock-"、`EncryptDogFrame` 工作线程名 "encrypt-dog-gui-worker" 改为 "encryptdog-" 前缀(非用户可见,纯一致性)

不包含:上游仓库名 `gaoxianglong/encryption-dog` 与 README 中指向其的 logo 外链(外部现实,无法从此仓库改名)、仓库目录名 `encrypt-dog-new`(git 无法改目录名)、用户本机的 `dog` 别名名称、版本号(维持 2.0.4)、历史归档 change 内的旧名(历史记录不改写)。

## Capabilities

### New Capabilities

- `branding`:应用品牌命名规范——品牌名称为 EncryptDog 在窗口标题栏、CLI 命令名、Maven/jar 产物名与 README 文档中的统一呈现。

### Modified Capabilities

- `swing-gui`:品牌名称文本由 "EncryptionDog" 更新为 "EncryptDog",并同步 jar 占位引用 `dog.jar` → `encryptdog.jar`(涉及启动参数路由、标题栏 logo 与品牌名称协调、信息层级三个 requirement,行为不变仅文本变更)。

## Impact

- 代码:`src/main/java/com/gxl/encryptdog/gui/interfaces/swing/constant/UiConstants.java`、`core/shell/EncryptDogConsole.java`、`Starter.java`、`gui/interfaces/swing/EncryptDogFrame.java`
- 构建:`pom.xml`(finalName、artifactId)、`build-mac.sh`(jar 复制路径)、`dependency-reduced-pom.xml` 与 `target/` 产物随构建重新生成
- 文档:`README.md`
- 用户可见行为:窗口标题栏、终端 usage 输出、jar 文件名;.app/.dmg 名不变
- 部署习惯:用户本机 `dog` 别名指向的 jar 路径需同步更新(构建后提醒同步 jar 的习惯不变)
