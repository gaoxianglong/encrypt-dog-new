# 设计：托盘守护拉起的 Java 回退探测

## Context

`TrayIpc.spawnJvm` 当前用 `java.home/bin/java` 定位 Java 可执行文件。`mac-app-packaging` 实测发现 jpackage 的 macOS app-image 会裁掉 bundle 内运行时（`Contents/runtime/Contents/Home`）的 `bin/`，`.app` 形态下该路径不存在 → 守护拉不起 → 静默无托盘（既有 D7 降级）。jar 形态不受影响。项目仅支持 macOS。

动机与范围见 proposal.md；需求见 specs/menu-bar-tray/spec.md。

## Goals / Non-Goals

**Goals:**

- .app 与 jar 两种形态下守护/GUI 拉起都可用
- 全部候选不可用时保持既有静默降级，零崩溃

**Non-Goals:**

- 不改守护协议/端口/单例逻辑
- 不做 Windows/Linux 适配（候选链本身跨平台无害，但 `java_home` 仅 macOS 探测）

## Decisions

### D1：候选链依次探测，取第一个存在且可执行者

```
候选1: <java.home>/bin/java            (jar 形态,现状路径,优先命中零变化)
候选2: /usr/libexec/java_home -v 21 解析的 JDK + /bin/java
        (macOS 系统工具,输出 JDK 路径;java_home 不存在或执行失败则跳过)
候选3: PATH 中 java (遍历 PATH,纯文件系统判定)
候选4: 标准JDK安装目录扫描: /Library/Java/JavaVirtualMachines、
        ~/Library/Java/JavaVirtualMachines、~/jdk 的 <jdk>/Contents/Home/bin/java
        (实测用户JDK在~/jdk,既未注册java_home也不在PATH,目录扫描兜底)
```

候选 2/3/4 均须通过 `-version` 大版本校验（≥15）；全部不可用 → 返回 null → 既有静默降级。探测实现为一个 `resolveJavaBinary()` 私有方法，`spawnJvm` 调用；守护（`spawnDaemon`）与 GUI（`spawnGui`）共用。

全部不可用 → 返回 null → 既有静默降级。探测实现为一个 `resolveJavaBinary()` 私有方法，`spawnJvm` 调用；守护（`spawnDaemon`）与 GUI（`spawnGui`）共用。

### D2：java_home 的调用与超时 + 候选版本校验（实测修正）

`ProcessBuilder("/usr/libexec/java_home", "-v", "21").start()` 读 stdout 首行（约 200ms）；用 3s 超时保护（`waitFor(3, SECONDS)` + 超时 destroy）。**实测发现**：本机 `java_home -v 21` 竟返回 `/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home`（Apple Java 8 遗留插件 JRE），拉起的守护因 class 文件版本不兼容（v59 vs 最高 52）秒崩——`-v` 过滤并不可靠。因此候选 2/3 的 java 均须通过**大版本校验**：执行 `<candidate> -version` 解析首行版本号（兼容 1.x 旧格式），大版本 ≥ 15（与 pom `java.version=15` 编译目标一致）才采用；3s 超时、失败跳过。候选 1（当前 JVM 自身）天然兼容，免校验。

### D3：候选 3 用 PATH 遍历而非 shell

`System.getenv("PATH")` 按 `:` 分割遍历 `java` 文件（`File.isFile() && canExecute()`），避免 shell 依赖与转义问题；与候选 1/2 一致的纯文件系统判定。

### D4：诊断日志

每个候选探测失败不打日志（常态静默）；全部失败时打一条 `[tray] 未找到可用 Java,托盘守护不可用`，便于 .app 场景排查（输出走既有 System.err，jpackage app 会进统一控制台日志）。

## Risks / Trade-offs

- [候选 2/3 拉起的外部 JVM 与 bundle 内运行时版本差异] → 守护仅跑 SystemTray + socket，无业务逻辑，JDK 大版本差异无影响。
- [`java_home -v 21` 在只有更高版本 JDK 的机器上失败] → 候选 3（PATH java）兜底；再不行静默降级，与现状一致。
- [外部 JVM 拉起的守护持有 bundle 内 jar 句柄] → 只读打开 classpath，无锁冲突；jar 随 .app 移动场景与 jar 形态同风险等级。
- [探测链本身耗时] → 候选 1 命中时零开销；java_home 探测仅在候选 1 缺失（.app 形态）时发生，一次性 ~200ms，在 ensureDaemon 3s 轮询预算内。

## Migration Plan

纯运行时路径增强：jar 形态行为不变（候选 1 优先），.app 形态从"无托盘"变为"有托盘"；无数据/配置迁移。回滚即还原 spawnJvm 单一候选实现。
