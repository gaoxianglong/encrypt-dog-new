# 实现任务：macOS App 打包脚本 build-mac.sh

## 1. 脚本实现

- [x] 1.1 新增项目根 `build-mac.sh`：`set -euo pipefail`；前置检查 `jpackage`/`iconutil` 可用；`mvn clean package -DskipTests`；版本号经 `mvn help:evaluate -Dexpression=project.version -q -DforceStdout` 提取。验证：脚本可执行（chmod +x）、bash 语法检查 `bash -n build-mac.sh` 通过。
- [x] 1.2 icns 生成：`sips -z` 从 `src/main/resources/dock_logo.png` 生成 16/32/128/256/512 及 @2x 共 10 档到临时 iconset，`iconutil -c icns` 打包，用后清理。验证：icns 文件生成且 `iconutil` 不报错。
- [x] 1.3 jpackage 调用：`--type app-image --name EncryptDog --app-version <版本> --main-class com.gxl.encryptdog.Starter --main-jar dog-<版本>.jar --arguments "--gui" --java-options "-Xms1g -Xmx1g -Xmn384m" --icon <icns> --input target --dest dist`；运行前清理 `dist/`（幂等）；可选参数 `dmg` 时追加 `--type dmg` 产出。验证：`./build-mac.sh` 完整跑通，`dist/EncryptDog.app` 存在。

## 2. 产物验收

- [x] 2.1 双击/`open dist/EncryptDog.app`：应用启动进入 GUI 表单页，进程名与 Dock 图标为 EncryptDog（icns 生效，非咖啡杯）。验证：与 dog --gui 行为一致。
- [x] 2.2 **托盘守护在 bundle 内实测**（设计 D5 风险项）：从 .app 启动后菜单栏出现 logo 图标；✕ 关窗图标保留；托盘 Open 重新拉起 GUI；Quit 全退。验证：bundle 内运行时含 bin/java 时自拉起逻辑照常工作；若守护拉不起，记录现象暂停（后续变更加回退探测，本次不动代码）。
- [x] 2.3 单例互斥：.app 运行中再跑 `dog --gui`（或反之），窗口只保留一个、带前台唤醒。验证：与既有单例语义一致。
- [x] 2.4 `./build-mac.sh dmg` 产出 `dist/EncryptDog-<版本>.dmg`。验证：dmg 可挂载、app 可拖入 Applications。
- [x] 2.5 终端模式与既有 jar 入口回归：`java -jar dog-2.0.4.jar -e ...` 加/解密行为不变（打包脚本不影响任何运行时路径）。
