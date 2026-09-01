# menu-bar-tray Spec Delta

## MODIFIED Requirements

### Requirement: 托盘守护进程生命周期

系统 SHALL 在 GUI 模式启动时确保托盘守护进程运行：探测本地守护端口，未运行时以低内存配置（`-Xmx64m`）拉起同一 jar 的 `--tray` 模式守护进程（Java 可执行文件 SHALL 按候选链探测：当前 JVM 的 java → 系统 JDK → PATH 中的 java，全部不可用时静默降级为无托盘）；守护已在运行时 SHALL NOT 重复拉起。守护 SHALL 仅挂载菜单栏图标，SHALL NOT 创建窗口、SHALL NOT 运行任何渲染动画或定时器。终端模式（不带 `--gui`）SHALL NOT 启动或触碰托盘守护，行为与引入托盘前完全一致。

#### Scenario: 首次 GUI 启动拉起守护

- **WHEN** 用户首次以 `--gui` 启动且无守护进程运行
- **THEN** GUI 自动拉起守护进程，菜单栏出现 logo 图标，随后 GUI 窗口正常显示

#### Scenario: 守护已在时不重复拉起

- **WHEN** 守护进程已在运行（图标已在菜单栏），用户再次以 `--gui` 启动
- **THEN** 系统不重复拉起守护，菜单栏图标保持唯一

#### Scenario: App 形态启动拉起守护

- **WHEN** 用户双击打包后的 EncryptDog.app 启动（bundle 内运行时不含 bin/java）
- **THEN** 系统经候选链找到系统 JDK 或 PATH 中的 java 并拉起守护，菜单栏出现 logo 图标，GUI 功能正常；系统中无任何可用 java 时静默无托盘，GUI 正常使用

#### Scenario: 终端模式无托盘

- **WHEN** 用户以不带 `--gui`/`--tray` 的方式启动（如 `java -jar dog.jar -e -s xxx -k yyy`）
- **THEN** 不出现托盘图标、不启动守护进程，终端行为与引入托盘前完全一致
