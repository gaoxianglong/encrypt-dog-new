# swing-gui Delta Spec

## MODIFIED Requirements

### Requirement: 启动参数路由

系统 SHALL 在启动时根据参数选择运行模式：包含 `--gui` 参数时启动 Swing 图形界面，否则走原有终端交互路径。

#### Scenario: GUI 模式启动

- **WHEN** 用户以 `java -jar encryptdog.jar --gui` 方式启动
- **THEN** 系统打开 Swing 图形窗口，不在终端执行 picocli 命令行解析

#### Scenario: 托盘守护模式启动

- **WHEN** 用户以 `--tray` 参数启动
- **THEN** 系统不再识别该参数：不进入任何守护模式、不挂载菜单栏图标，按无 `--gui` 的终端路径由 picocli 解析处理

#### Scenario: 终端模式启动

- **WHEN** 用户以不带 `--gui` 的方式启动（如 `java -jar encryptdog.jar -e -s xxx -k yyy`）
- **THEN** 系统行为与 GUI 功能引入之前完全一致，包括参数解析、Banner、确认交互与 Dashboard 渲染

### Requirement: 标题栏 logo 与品牌名称协调

标题栏 logo 的显示尺寸 SHALL 与品牌名称视觉高度相当，不显过小。

#### Scenario: 标题栏 logo 与品牌名称协调

- **WHEN** 用户查看窗口标题栏
- **THEN** logo 与 "EncryptDog" 垂直居中对齐，且 logo 视觉高度与品牌名称相当

### Requirement: 信息层级

GUI SHALL 采用"应用品牌—任务操作"两级信息层级：应用品牌（logo + EncryptDog）仅出现在窗口标题栏；主内容区 SHALL 为干净统一的半透明毛玻璃蒙层，SHALL NOT 绘制顶部高光带（不出现把蒙层分成上下两部分的横向分割线），蒙层上 SHALL NOT 展示任务主标题与副标题，首行即 Encrypt/Decrypt 模式切换，不重复展示品牌名称；版本号 SHALL 位于内容卡片右下角，以次要视觉样式展示。

#### Scenario: 品牌单一性

- **WHEN** 用户查看 GUI 窗口
- **THEN** logo 与 "EncryptDog" 仅在窗口标题栏出现一次，内容卡片不重复展示品牌名称

#### Scenario: 任务标题与模式切换

- **WHEN** 用户查看内容卡片并切换 Encrypt/Decrypt 模式
- **THEN** 卡片顶部不再展示任务主标题与副标题，首行直接为 Encrypt/Decrypt 分段选择器，模式切换仅联动确认密钥行与模式文案

#### Scenario: 版本号位置

- **WHEN** 用户查看内容卡片
- **THEN** 版本号显示在卡片右下角，为小字号次要色，不处于视觉焦点位置
