## Why

项目 pom 版本已升级到 2.0.4，但 GUI 首页右下角版本号仍显示 "v2.0.3-RELEASE"（UiConstants 硬编码），终端启动横幅同样读取 dog.properties 中的旧值 2.0.3-RELEASE。版本号应来自构建版本，而非多处硬编码，否则每次发版都会漂移。

## What Changes

- dog.properties 的 `project.version` 改为 `${project.version}`，由 Maven 构建时过滤注入 pom 版本（2.0.4）。
- pom.xml 仅对 `properties/**/*.properties` 开启资源过滤（整目录过滤会损坏 png 等二进制资源）。
- `UiConstants.VERSION` 取消硬编码，运行时从 dog.properties 读取 `project.version` 并加 "v" 前缀展示；构建版本升级后 GUI 与终端横幅自动一致，无需改代码。
- 终端启动横幅（BannerInfo 已读取 dog.properties）随之显示 2.0.4，无需改动。

## Capabilities

### New Capabilities

- `swing-gui`: 新增「版本号与构建版本一致」要求。

### Modified Capabilities

<!-- 无修改能力 -->

## Impact

- `pom.xml`: 增加 resources 过滤配置（仅 properties 文件）。
- `src/main/resources/properties/dog.properties`: `project.version=${project.version}`。
- `gui/interfaces/swing/constant/UiConstants.java`: VERSION 改为运行时读取。
- `core/shell/BannerInfo.java` 无需改动（已读取同一资源）。
- 不涉及加/解密逻辑与终端交互行为。
