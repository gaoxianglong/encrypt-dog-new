# 托盘守护拉起的 Java 回退探测

## Why

`mac-app-packaging` 变更实测发现：jpackage 在 macOS 上会裁掉 .app bundle 内运行时（`Contents/runtime/Contents/Home`）的 `bin/java`，导致 GUI 从 .app 形态启动时 `TrayIpc.spawnJvm` 无法拉起托盘守护（`java.home/bin/java` 不存在），菜单栏托盘缺失（按既有降级逻辑静默无托盘）。jar 入口不受影响。需要给守护/GUI 进程拉增加 Java 可执行文件的回退探测链，使 .app 形态同样具备完整托盘能力。

## What Changes

- `TrayIpc.spawnJvm` 的 Java 可执行文件定位改为**候选链探测**，依次取第一个存在且可执行的：
  1. `java.home/bin/java`（常规 `java -jar` 运行形态，现状）
  2. `/usr/libexec/java_home -v 21` 解析出的 JDK `bin/java`（.app 形态下系统的 JDK）
  3. PATH 中的 `java`（`command -v java`）
- 全部候选不可用时保持既有静默降级（无托盘，GUI 正常）。
- 守护与 GUI 两路拉起共用同一逻辑，.app 与 jar 两种形态行为一致。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

- `menu-bar-tray`: 「托盘守护进程生命周期」需求扩展：GUI 以 .app 形态启动时同样 SHALL 能拉起托盘守护（Java 可执行文件探测链兜底）；新增场景「App 形态启动拉起守护」。

## Impact

- 受影响代码：`TrayIpc.java`（spawnJvm 候选链，约 20 行）。
- 无新依赖；`/usr/libexec/java_home` 为 macOS 系统工具（项目仅支持 macOS）。
- 终端模式零影响；jar 形态行为不变（候选 1 优先命中）。
