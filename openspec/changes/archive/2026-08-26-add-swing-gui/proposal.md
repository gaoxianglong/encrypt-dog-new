# Add Swing GUI

## Why

加密狗目前只有终端交互方式，使用门槛较高（需要记忆命令行参数、通配符规则、Y/N 确认流程）。本项目需要一个现代化的 Swing GUI 前端，以"星空连线 + 毛玻璃卡片"的视觉风格降低使用门槛，同时**完全保留**现有终端模式——启动时通过参数选择进入哪种模式。

## What Changes

- 新增 `--gui` 启动参数路由：带 `--gui` 启动 Swing 图形界面，不带则走原有 picocli 终端路径，终端行为零变化。
- 新增 GUI 应用层：表单收集参数（文件多选、密钥 + 确认密钥、算法下拉、加密/解密模式、删除源文件、仅本机、目标目录）→ 解析 → 确认对话框 → 后台执行 → 实时进度 → 结果面板。
- 移植 test-demo 的视觉组件：星空粒子背景（星座连线、鼠标微扰）、毛玻璃卡片、渐变按钮、自绘标题栏、淡入淡出/抖动动画。**加密完成触发粒子聚集爆发动画，失败触发卡片抖动**。
- 界面规范：GUI 文案全英文、无 emoji 与装饰符号、状态以颜色区分；密码框内嵌眼睛图标支持明文/掩码切换；盾牌 logo 用于表单头部与标题栏。
- 核心代码三处纯增量微调（终端模式行为完全不变）：
  - `View` 接口新增 default 方法 `isConsoleRequired()`（默认 true）；
  - `ViewSchedule.renderDashboardView` 允许非终端环境下渲染 `isConsoleRequired() == false` 的视图；`stop()` 重建调度线程池以支持 GUI 会话内多次操作；
  - `EncryptProxy` 新增 `setView(View)`，缺省仍是 `DashboardView`。
- pom.xml 构建依赖调整：新增 flatlaf 3.7.1（Java 8+ 兼容，当前 Java 15 无需升级）；maven-shade 升级至 3.5.2；处理 flatlaf 传递引入的 slf4j 与现有 logback 1.2.3 的冲突。

## Capabilities

### New Capabilities

- `swing-gui`: Swing 图形界面能力——启动路由、表单交互、进度与结果展示、星空粒子视觉主题，以及 GUI 模式与核心加解密引擎的协作方式。

### Modified Capabilities

（无。终端模式行为不发生任何变化，本仓库尚无已存在的 openspec 规格。）

## Impact

- **代码**：新增 `com.gxl.encryptdog.gui` 包（interfaces/application/infrastructure-acl 分层）；微调 `View`、`ViewSchedule`、`EncryptProxy` 三个核心类（纯增量，见 What Changes）；`Starter` 增加 `--gui` 参数剥离与路由（原有路径不动）。
- **依赖**：新增 `com.formdev:flatlaf:3.7.1`；升级 `maven-shade-plugin` 2.3 → 3.5.2；slf4j/logback 版本冲突需在 pom 中处理。
- **终端模式回归面**：加解密算法、执行器、事件监听链、Dashboard 渲染、Y/N 确认流程全部不动，需全量回归验证。
