# Design: Rename to EncryptDog

## Context

动机见 proposal.md - Why。现状:品牌串散落在四个形态——标题栏 `UiConstants.APP_NAME="EncryptionDog"`、CLI `@Command(name = "encrypt-dog")`、jar `finalName=dog-${project.version}`、README 混用;而 `build-mac.sh` 的 `APP_NAME="EncryptDog"` 已是新名(.app/.dmg 无需改动)。pom artifactId 为 `encrypt-dog-new`,版本 2.0.4。`dependency-reduced-pom.xml`、`target/`、`dist/`、`.iml`、`dec.log` 均为 gitignored 生成物。

## Goals / Non-Goals

**Goals:**

- 全部用户可见命名表面收敛到 EncryptDog,且 jar 基名、CLI 命令名、品牌串三者无连字符/驼峰混用
- 内部标识字符串(线程名、临时文件前缀)顺带统一

**Non-Goals:**

- 不改版本号(维持 2.0.4)、不改 `.app`/`.dmg` 名(已是 EncryptDog)、不改上游仓库名与指向它的外链、不改仓库目录名 `encrypt-dog-new`(git 无法改目录)、不改归档 change 历史文本

## Decisions

### D1: CLI 命令名取 `encryptdog`(无连字符)

picocli `@Command(name=...)` 由 `encrypt-dog` 改为 `encryptdog`,与 jar 基名 `encryptdog` 完全一致——usage 输出、帮助、jar 文件名三处对齐,脚本按 `Usage: encryptdog` 匹配也不会因文件名差异错位。

- 备选:保留 `encrypt-dog` → 否决,与 jar/品牌串三态并存正是本变更要消灭的问题
- 备选:改名 `EncryptDog`(驼峰)→ 否决,CLI 命令名惯例小写无空格(与 picocli 默认用 jar 名小写的风格一致)

### D2: jar 与 artifactId 同步改

`pom.xml`:`<finalName>dog-${project.version}</finalName>` → `<finalName>encryptdog-${project.version}</finalName>`;`<artifactId>encrypt-dog-new</artifactId>` → `<artifactId>encryptdog</artifactId>`。artifactId 改名会让 `target/` 目录名变化,但 `target/`、`dependency-reduced-pom.xml` 均为 gitignored 生成物,重建自愈;`.iml` 亦 gitignored。

- 备选:只改 finalName 保留 artifactId → 否决,"全部统一"要求 artifactId 不再携带 `-new` 历史后缀

### D3: build-mac.sh 只动 jar 复制路径

`APP_NAME="EncryptDog"` 不动;仅第 78 行 `cp "target/dog-${VERSION}.jar"` → `cp "target/encryptdog-${VERSION}.jar"`,与 D2 的 finalName 联动(脚本已有 VERSION 解析逻辑,无需改动)。

### D4: 标题栏品牌串走单点常量

只改 `UiConstants.APP_NAME` 一处("EncryptionDog" → "EncryptDog"),`TitleBar` 经 `JLabel(UiConstants.APP_NAME)` 引用,自动生效。apply 时 grep 验证无其他硬编码品牌串。

### D5: README 外链保留、下载链接标注

logo/hs.png 外链指向 `gaoxianglong/encryption-dog` master——外部资源,改名会断链,保留。`encrypt-dog-new.git` clone 链接与 GitHub 仓库实际名一致,保留。`releases/download/v2.0.4/dog-2.0.4.jar` 更新为 `encryptdog-2.0.4.jar` 后,该资产在仓库重新发布 release 前不存在(见风险 R2)。

### D6: 主 spec 文本经 delta 同步,不在 apply 期直接改主 spec

`openspec/specs/swing-gui/spec.md` 中 "EncryptionDog" 与 "dog.jar" 占位文本由本 change 的 swing-gui delta 覆盖,在 sync/archive 阶段合并进主 spec;apply 阶段不直接编辑主 spec 文件。

## Risks / Trade-offs

- **[R1] 遗漏字符串残留** → apply 任务含收尾 grep 验证:代码/README/构建脚本全文无 `EncryptionDog`、`encrypt-dog`、`dog-<version>.jar` 残留(上游外链与归档 change 除外)
- **[R2] README release 下载链接指向尚不存在的资产** → README 更新链接后注明需在下次发布时上传 `encryptdog-2.0.4.jar` 资产;本地构建安装路径仍以 `mvn package` 为主
- **[R3] 用户本机 `dog` 别名与部署习惯受影响** → 别名名不变,但别名内的 jar 路径需随新 jar 名更新;按既有习惯在构建后提醒用户同步 jar 到部署位置
- **[R4] CLI 命令名变化破坏依赖 `encrypt-dog` usage 匹配的外部脚本** → 属 proposal 已标注的 BREAKING;本仓库内无此类脚本(grep 验证),外部影响以 README 文档提示
