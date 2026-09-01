# 设计：macOS App 打包脚本 build-mac.sh

## Context

项目为 Maven fat jar（shade，`dog-2.0.4.jar`，mainClass `Starter`），JDK 21（含 `jpackage`/`jlink`/`iconutil` 生态）。用户运行入口为 `dog` 别名（`java -Xms1g -Xmx1g -Xmn384m -jar ~/Desktop/Desktop/dog-2.0.4.jar --gui`）。图标资源 `src/main/resources/dock_logo.png`（1254×1254）。托盘守护由 GUI 以 `java.home/bin/java -Xmx64m -jar <self-jar> --tray` 自拉起——打包进 .app 后 `java.home` 指向 bundle 内运行时，`bin/java` 是否存在需实测。

动机与范围见 proposal.md；本变更 skip_specs，无 spec delta。

## Goals / Non-Goals

**Goals:**

- 一条命令（`./build-mac.sh`）产出可直接双击运行的 `EncryptDog.app`
- .app 行为与 `dog --gui` 完全一致（同 jar、同 JVM 参数、同启动参数路由）
- 可选产出 `.dmg` 供分发

**Non-Goals:**

- 不做 AOT 原生编译（Swing 生态不支持，探索已定案）
- 不做代码签名/公证（自用场景；分发他人另议 Apple Developer 账号）
- 不改任何 Java 代码与运行时行为

## Decisions

### D1：`jpackage --type app-image` 为默认产物，`--dmg` 为可选二级产物

app-image 产出可运行 .app 目录；dmg 在其基础上做安装镜像。脚本接受一个可选参数 `dmg` 控制二级产物，默认只出 .app（构建快、自用直接拖进 Applications）。

### D2：icns 生成 = `sips` 多档缩放 + `iconutil`

`iconutil -c icns` 需要 iconset 目录结构（16/32/128/256/512 及 @2x 共 10 档）；脚本用 `sips -z` 从 dock_logo.png 生成各档后打包 icns。dock_logo.png 为方形且远大于 1024，缩放无质量风险。临时 iconset 放 `/tmp` 或 `dist/.iconset`（用完删除）。

### D3：jpackage 关键参数

```
--name EncryptDog --app-version <pom版本>
--main-class com.gxl.encryptdog.Starter --main-jar dog-<版本>.jar
--arguments "--gui"          (双击即 GUI 模式,与用户入口一致)
--java-options "-Xms1g -Xmx1g -Xmn384m"   (与 dog 别名一致)
--icon EncryptDog.icns       (Info.plist 原生 Dock 图标,运行时hack变冗余兜底)
--input target --dest dist
```

版本号从 pom 提取（`mvn help:evaluate -Dexpression=project.version -q -DforceStdout`），避免脚本与 pom 双份维护。

### D4：脚本幂等

每次运行先 `rm -rf dist/`（jpackage 目标目录存在会失败），重建全链路；`set -euo pipefail` 快速失败；脚本无 `dmg` 参数时跳过 dmg 阶段。

### D5：托盘守护在 bundle 内的运行方式（风险项，不动代码）

`.app` 内运行时位于 `EncryptDog.app/Contents/runtime/Contents/Home`，含 `bin/java`（jpackage 保留标准启动器，仅裁剪部分工具）。GUI 自拉起守护的 `java.home/bin/java` 逻辑预计照常工作——**验收任务实测**：从 .app 启动 → 菜单栏图标出现 → ✕ 关窗图标保留 → Quit 全退。若 bin/java 缺失（某些 JDK 版本裁剪策略差异），后续变更加回退逻辑（依次探测 bundle 运行时 bin/java → PATH 中 java），本次不改代码。

## Risks / Trade-offs

- [bundle 内运行时缺 bin/java 导致守护拉不起] → 验收实测；若命中，降级为后续变更加回退探测，.app 主体功能（GUI/加密）不受影响。
- [产物体积 ~70-90MB] → 捆绑运行时的固有成本，换取"用户无需装 Java"。
- [未签名 app 在他人 Mac 上被 Gatekeeper 拦截] → 自用无感；分发需签名公证，记录为未来项。
- [jpackage 依赖的 JDK 工具在 CI/其它机器缺失] → 脚本前置检查 `command -v jpackage iconutil`，缺失即报错退出并提示。

## Migration Plan

无运行时迁移。老用户继续 `java -jar`/`dog` 别名，新入口 .app 与老入口行为一致；两者可并存（单例锁会互斥唤醒同一窗口）。
