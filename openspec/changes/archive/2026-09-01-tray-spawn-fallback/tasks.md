# 实现任务：托盘守护拉起的 Java 回退探测

## 1. 候选链实现

- [x] 1.1 `TrayIpc` 新增 `resolveJavaBinary()` 候选链：`java.home/bin/java` → `/usr/libexec/java_home -v 21`（3s 超时、失败跳过）→ PATH 遍历 `java` → 标准 JDK 目录扫描（`/Library/Java/JavaVirtualMachines`、`~/Library/Java/JavaVirtualMachines`、`~/jdk`），候选 2/3/4 须通过 `-version` 大版本校验（≥15，实测 java_home 返回 Java 8 遗留插件 JRE 导致守护秒崩、用户 JDK 未注册 java_home），返回第一个存在、可执行且版本满足的路径；全部不可用返回 null 并打一条诊断日志。验证：`mvn compile` 通过。
- [x] 1.2 `spawnJvm` 改用 `resolveJavaBinary()`；守护与 GUI 两路共用（spawnDaemon/spawnGui 不改签名）。验证：`mvn compile` 通过；jar 形态行为不变（候选 1 命中）。

## 2. 验收与回归

- [x] 2.1 重新打包（`./build-mac.sh`）并双击 .app：菜单栏出现 logo 图标（候选 2 命中系统 JDK），✕ 关窗图标保留，托盘 Open 拉起 GUI，Quit 全退。验证：与 spec 场景「App 形态启动拉起守护」一致。
- [x] 2.2 jar 形态回归：`java -jar dog.jar --gui` 守护照常拉起（候选 1 命中），行为与改动前一致。
- [x] 2.3 终端模式回归：不带 `--gui`/`--tray` 执行一次加/解密，行为不变。
