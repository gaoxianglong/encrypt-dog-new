# macOS App 打包脚本（build-mac.sh）

## Why

当前用户以 `java -jar dog.jar --gui` 方式运行：需要 JDK 环境、shell 别名，Dock 图标依赖运行时 hack，无法双击运行。用户希望"一键把 jar 打包成 APP 直接运行"。探索结论：AOT 原生编译对 Swing 应用不可行（GraalVM Native Image 不支持 java.desktop），但 JDK 自带的 `jpackage` 可以把 fat jar 打包成真 `.app`（捆绑精简运行时、原生 Dock 图标、双击运行、用户无需安装 Java）。本变更固化一个 `build-mac.sh` 打包脚本。

## What Changes

- 新增项目根 `build-mac.sh` 打包脚本：
  - 执行 `mvn clean package -DskipTests` 产出 fat jar
  - 用 `iconutil` 从 `dock_logo.png`（1254×1254 现成资源）生成 `.icns`（16~1024px 全套 @1x/@2x）
  - `jpackage --type app-image` 产出 `EncryptDog.app`（主类 `Starter`、启动参数 `--gui`、JVM 参数与 dog 别名一致 `-Xms1g -Xmx1g -Xmn384m`、应用版本随 pom 版本）
  - 可选 `--dmg` 参数产出 `.dmg` 安装镜像
- 产物输出到 `dist/`（不污染 target）；重复运行自动清理旧产物。
- 纯工具链变更：**不改变任何运行时行为**（同一 jar、同一参数路由），故声明 `skip_specs`。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

无（构建工具链变更，无 spec 级行为变化，已声明 `skip_specs: true`）。

## Impact

- 新增文件：`build-mac.sh`（项目根）。
- 复用资源：`src/main/resources/dock_logo.png`（icns 源图）、pom 版本号。
- 无 Java 代码变更、无依赖变更、无终端/GUI 行为变更。
- 注意点（脚本外风险）：打包成 .app 后，托盘守护自拉起依赖 bundle 内运行时的 `bin/java`，需在验收中实测确认。
