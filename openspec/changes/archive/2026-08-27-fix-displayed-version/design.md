## Context

版本号当前有 3 处来源且互相脱节:pom.xml=2.0.4;dog.properties `project.version=2.0.3-RELEASE`(core BannerInfo 读取,终端横幅显示旧值);UiConstants.VERSION="v2.0.3-RELEASE" 硬编码(GUI 右下角显示旧值)。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 单一版本源 = pom 版本,构建时注入,运行时读取,GUI 与终端横幅展示一致。
- 下次发版只改 pom,展示自动跟随。

**Non-Goals:**

- 不改版本号展示位置、样式与终端交互行为。

## Decisions

### D1: 资源过滤仅覆盖 properties 文件

pom `<resources>` 增加两条:整目录不过滤(默认复制,保护 logo.png 等二进制资源),叠加一条仅对 `properties/**/*.properties` 开启 filtering。备选:整目录开启过滤 → 二进制资源可能被文本编码处理损坏,放弃。

### D2: dog.properties 使用 `${project.version}` 占位

构建时 Maven 将 pom 版本(2.0.4)注入。BannerInfo 无需改动即显示新值。

### D3: UiConstants.VERSION 运行时读取 dog.properties

静态初始化时从 classpath 读取 `properties/dog.properties` 的 `project.version`,拼 "v" 前缀;读取失败回退 "vUNKNOWN"。备选:仅把常量改成 2.0.4 → 下次发版再次漂移,放弃。

## Risks / Trade-offs

- [读取失败显示 vUNKNOWN] → 仅在打包异常时出现,正常 jar 内必含过滤后的 properties。
- [过滤改变 properties 文件编码] → Maven 默认 UTF-8 输出,properties 为纯 ASCII,无影响。

## Migration Plan

构建配置与资源变更;回滚即恢复硬编码与静态值。需 `mvn clean package` 全量重打包验证。
