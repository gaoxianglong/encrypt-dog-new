# 实现任务：彻底移除菜单栏托盘功能

## 1. 代码移除与回退

- [x] 1.1 删除 `gui/interfaces/swing/tray/` 整个包（TrayDaemon.java、TrayIpc.java）。验证：`mvn compile` 通过（Starter 回退后无引用残留）。
- [x] 1.2 `Starter.java` 回退：移除 `--tray` 路由、`TRAY_OPTION` 常量、TrayDaemon import、parseGuiArgs 的 TRAY_OPTION 排除；保留 `preinstallDockIcon`。验证：`mvn compile` 通过；grep `tray|Tray` 仅剩注释/Dock 相关为零残留。
- [x] 1.3 `EncryptDogGui.java` 回退：移除单例锁（acquireSingleton/serveCommands/guiServer/静态 frame 字段）、ensureDaemon 调用与 tray 相关 import（TrayIpc/Frame/WindowEvent/ServerSocket/Socket/IOException/PrintWriter/StandardCharsets），窗口创建回退为局部变量；保留 `installDockIcon`。验证：`mvn compile` 通过。
- [x] 1.4 残留检查：全仓库 grep `TrayIpc|TrayDaemon|32345|32346` 零命中（归档/历史目录除外）；`--tray` 仅允许出现在 openspec spec 文本（改写后的场景描述）与历史归档中，`src/` 下零命中。验证：无引用残留。

## 2. 验收与回归

- [x] 2.1 jar 形态：`java -jar dog.jar --gui` 启动——无守护进程（32345 无监听）、无菜单栏图标、Dock 盾牌图标正常；✕ 关窗进程退出。验证：与 spec「启动参数路由」「Dock 图标」一致。
- [x] 2.2 二次启动：再跑一个 `--gui` 会开第二个窗口（单例已删，用户确认的回退语义）。验证：行为与托盘引入前一致。
- [x] 2.3 重新打包 `./build-mac.sh`：.app 启动正常、无守护进程、Dock 图标为 icns；`./build-mac.sh dmg` 产物正常。验证：.app 与 jar 形态行为一致。
- [x] 2.4 终端模式回归：不带 `--gui` 执行一次加/解密，行为与改动前完全一致。
- [x] 2.5 GUI 常规功能回归：表单、执行页、状态徽章、拖拽、模式切换不受影响。
