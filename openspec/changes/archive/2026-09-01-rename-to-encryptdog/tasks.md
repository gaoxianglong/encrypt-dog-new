# Tasks: Rename to EncryptDog

## 1. 代码品牌串

- [x] 1.1 `UiConstants.APP_NAME` 由 "EncryptionDog" 改为 "EncryptDog"；验证：`grep -rn EncryptionDog src/` 无输出，`mvn compile` 通过
- [x] 1.2 `EncryptDogConsole` 的 `@Command(name = "encrypt-dog")` 改为 `encryptdog`；验证：grep 源码确认 `@Command(name = "encryptdog")` 生效且 `mvn compile` 通过（运行时 usage 检查见 4.2）
- [x] 1.3 `Starter` 临时文件前缀 "encrypt-dog-dock-" 改为 "encryptdog-dock-"、`EncryptDogFrame` 线程名 "encrypt-dog-gui-worker" 改为 "encryptdog-gui-worker"、`logback.xml` 日志路径 `~/logs/encrypt-dog/` 与 `encrypt-dog.log` 改为 encryptdog（用户确认的范围扩展）；验证：`grep -rn "encrypt-dog" src/` 无输出，`mvn compile` 通过（Dock 图标运行时检查见 4.2）
- [x] 1.4 `BannerInfo` 的 ASCII banner 由 figlet small_slant 字体的 "EncryptionDog" 重新生成为 "EncryptDog"（验收反馈的遗漏面），README terminal mode 段的 banner 示例同步；验证：`java -jar target/encryptdog-2.0.4.jar -h` 输出 banner 拼写 EncryptDog（small_slant 渲染匹配），无 "EncryptionDog" 形态

## 2. 构建配置

- [x] 2.1 `pom.xml`：`<finalName>dog-${project.version}</finalName>` 改为 `<finalName>encryptdog-${project.version}</finalName>`，`<artifactId>encrypt-dog-new</artifactId>` 改为 `<artifactId>encryptdog</artifactId>`；验证：`mvn clean package` 产出 `target/encryptdog-2.0.4.jar` 且 `target/` 下无 `dog-2.0.4.jar`（clean 清除旧构建遗留的 dog-2.0.4.jar，避免误报）
- [x] 2.2 `build-mac.sh` 第 78 行 `cp "target/dog-${VERSION}.jar"` 与第 83 行 `--main-jar "dog-${VERSION}.jar"` 改为 `encryptdog-${VERSION}.jar`（`APP_NAME` 已是 EncryptDog，不动）；验证：执行 `build-mac.sh` 产出 `dist/EncryptDog.app` 与 `dist/EncryptDog-2.0.4.dmg`

## 3. README

- [x] 3.1 README 品牌引用 "EncryptionDog" 改为 "EncryptDog"、`dog-2.0.4.jar` 全部改为 `encryptdog-2.0.4.jar`（含 alias、wget、`--gui` 示例）、usage 行改为 `Usage: encryptdog`、releases 下载链接改为 `encryptdog-2.0.4.jar`；验证：`grep -nE "EncryptionDog|(^|[^a-z])dog-2\.0\.4" README.md` 无输出（边界匹配，避免误中 encryptdog-2.0.4 子串），logo/hs.png 外链与 `encrypt-dog-new.git` clone 链接保持原样；注意 releases 下载资产需在下次发布时上传（本地安装以 `mvn package` 为主）

## 4. 验证收尾

- [x] 4.1 全文残留 grep：代码、README、构建脚本中无 `EncryptionDog`、`encrypt-dog([^-a-z]|$)`、`(^|[^a-z])dog-2.0.4` 残留（边界匹配避免误中 encryptdog-2.0.4 子串与保留的 `encrypt-dog-new` 仓库名 URL；上游 `encryption-dog` 外链、`openspec/changes/archive/` 历史记录、gitignored 生成物除外）；验证：grep 输出为空
- [x] 4.2 GUI 冒烟：`java -jar target/encryptdog-2.0.4.jar --gui` 启动，窗口标题栏显示 "EncryptDog"、Dock 显示应用图标且启动全程无咖啡杯图标；终端 `-h` 输出 `Usage: encryptdog`；验证：标题栏文本、Dock 图标与 usage 输出符合新名
- [x] 4.3 部署提醒：提醒用户 `dog` 别名指向的部署副本（~/Desktop/Desktop 旧副本）需同步新 jar 并更新别名内 jar 路径；验证：用户确认部署副本已替换为 `encryptdog-2.0.4.jar`（用户验收后决定本次不同步：部署副本与 /etc/profile 别名保持指向旧 jar，后续自行同步）
