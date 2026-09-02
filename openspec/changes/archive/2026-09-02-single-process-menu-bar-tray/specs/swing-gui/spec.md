## MODIFIED Requirements

### Requirement: 启动参数路由

系统 SHALL 在启动时根据参数选择运行模式：包含 `--gui` 参数时启动 Swing 图形界面，否则走原有终端交互路径。GUI 模式下已有实例正在运行时，重复 `--gui` 启动 SHALL 唤醒已有实例而非重复开窗。

#### Scenario: GUI 模式启动

- **WHEN** 用户以 `java -jar encryptdog.jar --gui` 方式启动
- **THEN** 系统打开 Swing 图形窗口，不在终端执行 picocli 命令行解析

#### Scenario: 托盘守护模式启动

- **WHEN** 用户以 `--tray` 参数启动
- **THEN** 系统不再识别该参数：不进入任何守护模式、不挂载菜单栏图标（菜单栏图标由 GUI 模式单进程内挂载，不由独立参数触发），按无 `--gui` 的终端路径由 picocli 解析处理

#### Scenario: 重复启动唤醒已有实例

- **WHEN** 已有 GUI 实例正在运行（窗口可见或隐藏），用户再次以 `--gui` 方式启动（jar 形态）
- **THEN** 新进程不打开第二个窗口、不挂载第二个菜单栏图标，向已有实例发送唤醒命令后自行退出，已有实例窗口被带回前台（含隐藏态恢复）

#### Scenario: 终端模式启动

- **WHEN** 用户以不带 `--gui` 的方式启动（如 `java -jar encryptdog.jar -e -s xxx -k yyy`）
- **THEN** 系统行为与 GUI 功能引入之前完全一致，包括参数解析、Banner、确认交互与 Dashboard 渲染

### Requirement: Dock 图标

GUI 模式运行期间，系统 SHALL 通过标准 Taskbar API 将 macOS Dock 图标设置为应用图标资源 `dock_logo.png`（以 128px + 256px@2x 双档多分辨率图像呈现，等比缩放不拉伸变形）。Dock 图标 SHALL 以圆角呈现（圆角半径约为图标内框边长的 22%，与 macOS 标准图标观感一致），图标内容 SHALL 收进约 82% 内框（与 macOS 标准图标自带约 18% 透明边距一致，避免 Dock 中观感偏大），启动预置、运行时设置与 .app 打包产物三处 SHALL 保持一致。GUI 启动初期 SHALL NOT 出现 Java 默认咖啡杯图标——系统 SHALL 在 AWT 初始化前将图标资源拷贝到临时文件（每次启动全新拷贝）并预置给 JVM。环境不支持 Taskbar 或预置失败时 SHALL 静默跳过/降级，SHALL NOT 崩溃或弹错。终端模式 SHALL NOT 设置 Dock 图标。窗口因 ✕ 关闭而隐藏期间，用户点击 Dock 图标 SHALL 唤回既有窗口（不重复创建窗口）。

#### Scenario: 运行期 Dock 展示应用图标

- **WHEN** 用户以 `--gui` 启动加密狗
- **THEN** macOS Dock 中该应用显示 dock_logo.png 图标（清晰不模糊、非方形等比缩放、非 Java 默认咖啡杯图标、圆角呈现约 22% 内框边长半径、视觉大小与其它系统应用相当不偏大）

#### Scenario: 启动期无咖啡杯闪现

- **WHEN** 用户以 `--gui` 启动加密狗并观察启动全过程
- **THEN** Dock 图标从应用出现起即为 dock_logo.png，全程不显示 Java 默认咖啡杯图标

#### Scenario: 图标圆角一致

- **WHEN** 用户分别以 jar 形态与 .app 形态运行加密狗并查看 Dock
- **THEN** 两种形态的 Dock 图标均以圆角呈现（约 22% 内框边长半径）、观感一致，启动全程无方形到圆角的跳变

#### Scenario: Taskbar 不可用静默降级

- **WHEN** 运行环境不支持 Taskbar（如无桌面环境的服务器）
- **THEN** 系统静默跳过 Dock 图标设置，GUI 其余功能正常，不崩溃、不弹错

#### Scenario: Dock 点击唤回隐藏窗口

- **WHEN** 窗口因 ✕ 关闭而隐藏，用户点击 Dock 中的 EncryptDog 图标
- **THEN** 既有窗口恢复前台显示（不重复创建窗口），停留在关闭前的页面

#### Scenario: 终端与托盘模式不设置

- **WHEN** 用户以终端模式（不带 `--gui`）运行
- **THEN** 该进程不设置 Dock 图标，行为与引入 Dock 图标前一致
