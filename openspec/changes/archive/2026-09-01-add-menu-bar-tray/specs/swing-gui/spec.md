# swing-gui Spec Delta

## MODIFIED Requirements

### Requirement: 启动参数路由

系统 SHALL 在启动时根据参数选择运行模式：包含 `--gui` 参数时启动 Swing 图形界面，并在启动流程中确保菜单栏托盘守护进程运行（详见 menu-bar-tray 能力）；包含 `--tray` 参数时启动托盘守护模式（无窗口，仅菜单栏图标）；否则走原有终端交互路径。

#### Scenario: GUI 模式启动

- **WHEN** 用户以 `java -jar dog.jar --gui` 方式启动
- **THEN** 系统打开 Swing 图形窗口，不在终端执行 picocli 命令行解析，并确保菜单栏托盘守护进程已运行（未运行则自动拉起）

#### Scenario: 托盘守护模式启动

- **WHEN** 用户（或 GUI 自动拉起）以 `--tray` 参数启动
- **THEN** 系统进入托盘守护模式：不创建窗口、不执行 picocli 解析、不进行加解密操作，仅在菜单栏挂载 logo 图标并提供 Open/Quit 菜单

#### Scenario: 终端模式启动

- **WHEN** 用户以不带 `--gui` 的方式启动（如 `java -jar dog.jar -e -s xxx -k yyy`）
- **THEN** 系统行为与 GUI 功能引入之前完全一致，包括参数解析、Banner、确认交互与 Dashboard 渲染
