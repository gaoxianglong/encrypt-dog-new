# Design: Swing GUI

## Context

现有执行链（见 proposal 动机，此处只列与本设计相关的约束）：

```
Starter → picocli → EncryptDogConsole.run()
   validate() → FileNameParser.parse() → confirmation(Y/N) → buildOperationVO()
   → EncryptProxy.invoke(OperationContext)
        CountDownLatch(文件数+1)
        ├─ 每文件 EncryptExecuter（缓存线程池，并行）
        ├─ ViewSchedule 每秒渲染 View（终端的 DashboardView）
        │     └─ 渲染任务在 latch 剩 1 时补最后一次 countDown
        ├─ latch.await()
        └─ FinishedEvent → 监听器链（进度/结果/提示音）
```

三个关键事实：

1. `Utils.isConsole()` 判断的是 `System.console() != null`（有没有 TTY），**不是用户选择的模式**。终端里启动 GUI 时它仍为 true。
2. 闭锁多出的 `+1` 完全依赖渲染任务的补 countDown。非 TTY 环境下 `renderDashboardView` 直接 return → `invoke()` 永久阻塞（现存的隐性死锁，GUI 必须绕开）。
3. `ViewSchedule` 是静态类且 `stop()` 执行 `shutdownNow()`，同一 JVM 内第二次 `invoke()` 会 RejectedExecutionException。GUI 天然是"一窗多操作"，必须支持重复执行。

约束：Java 15；maven-shade 2.3（老）；logback 1.2.3 + slf4j 1.7（老）；代码规范遵循 java-engineering-structure skill（DDD 分层、防腐层、新代码禁用 lambda）；终端模式可观察行为零变化。

## Goals / Non-Goals

**Goals:**

- GUI 与核心执行链完整复用（不复制加解密逻辑），三处核心微调均为纯增量、缺省行为不变。
- GUI 作为独立限界上下文，通过 ACL 防腐层访问 core，Swing 层不感知 core 类型。
- 支持同一 JVM 会话内多次操作。

**Non-Goals:**

- 不改动任何加解密算法、文件格式、状态机与事件监听链。
- 不改动 `EncryptDogConsole` 的编排逻辑（包括不抽取 `buildOperationVO` 公共类）。
- 不做跨平台扩展（保持"仅 macOS"约束）、不做 GUI 单元测试框架搭建（仅终端回归清单）。
- 不动 `Utils.isConsole()` 的既有语义。

## Decisions

### D1. 核心三处纯增量微调（接入缝）

- `View` 接口新增 `default boolean isConsoleRequired() { return true; }`：`DashboardView` 零改动继承缺省值；GUI 视图覆写为 false。
- `ViewSchedule.renderDashboardView` 首行条件由 `if (!Utils.isConsole()) return;` 改为 `if (!Utils.isConsole() && view.isConsoleRequired()) return;`：终端 + TTY 行为不变；GUI 视图在无 TTY 时照常被调度（它同时承担进度渲染与闭锁补 countDown 两个职责，与 DashboardView 在终端模式下的职责完全对称）。
- `ViewSchedule.stop()` 改为 `shutdownNow()` 后重建 executor（字段去掉 final）：终端模式下进程随后退出、行为不变；GUI 模式下支持多次操作。
- `EncryptProxy` 新增 `setView(View)`：缺省仍 `new DashboardView()`。

备选方案与拒绝理由：
- *GUI 复制整条执行链*：违背"不动核心逻辑"，且两份逻辑必然漂移。拒绝。
- *用 TTY 检测隐式切模式*：终端里启动 GUI 会同时触发终端 Dashboard 渲染与 `readPassword` 阻塞。拒绝。
- *让 GUI 自己再开一个 Swing Timer 轮询 ResultContext 并在完成后补 countDown*：`ResultContext` 是 `EncryptProxy` 私有字段且 latch 不可见，无法实现。拒绝。

### D2. GUI 包 DDD 分层（对齐 java-engineering-structure）

GUI 是独立限界上下文，core（`com.gxl.encryptdog.core`）视为外部上下文，**唯一接触点收敛在 ACL**：

```text
com.gxl.encryptdog.gui/
├── interfaces/swing/            # 表现层：纯 Swing 组件 + 常量，只依赖 application DTO 与回调接口
│   ├── EncryptDogFrame          # 无边框圆角主窗口（移植 demo LoginFrame）
│   ├── TitleBar                 # 自绘标题栏（拖拽/关闭）
│   ├── ParticlePanel            # 星空粒子背景（星座连线/鼠标微扰/聚集爆发）
│   ├── GlassCardPanel           # 毛玻璃卡片
│   ├── GradientButton           # 渐变按钮
│   ├── EncryptFormPanel         # 表单：文件/密钥/算法/选项
│   ├── ProgressPanel            # 逐文件进度条
│   ├── ResultPanel              # 结果摘要
│   └── constant/UiConstants     # 视觉与动画常量
├── application/
│   ├── service/EncryptOperationAppService   # 编排：表单DTO → 校验 → 解析 → 确认 → 执行 → 回显
│   └── dto/                     # EncryptFormDTO / OperationProgressDTO / OperationResultDTO / OperationListener
└── infrastructure/acl/EncryptCoreFacade     # 防腐层：唯一 import core 的地方
    ├── 表单DTO → ConsoleRequest/OperationVO 组装（含 ~25 行 glue 的等价实现）
    ├── 内部持有 GuiDashboardView implements core View（isConsoleRequired=false），
    │      draw() 中把 ResultContext 转成 OperationProgressDTO 经回调推给 application 层
    ├── 每次操作 new EncryptProxy() + setView + invoke（后台线程）
    └── invoke 返回后读取最终 ResultContext 转 OperationResultDTO
```

分层依赖规则：`interfaces/swing → application/dto、application/service → infrastructure/acl`。core 类型（`ConsoleRequest`/`OperationVO`/`ResultContext`/`EncryptProxy`）不出 ACL。domain 层不建：加密领域逻辑全部在 core 上下文，GUI 上下文无独立领域规则，避免空壳包。

### D3. `buildOperationVO` 等价实现在 ACL 内，不抽公共类

- 选中理由：零改动 `EncryptDogConsole`（用户约束"终端逻辑不受影响"），glue 仅 ~25 行且语义由 `OperationVO.setTargetFile/setSecretKey` 等模型方法承载，漂移风险集中在一个类内。
- 备选：抽 `OperationVOBuilder` 供终端与 GUI 共用——更 DRY，但要改终端类，违背约束。拒绝。

### D4. 每次操作 new EncryptProxy()

`ResultContext` 是 `EncryptProxy` 实例字段，复用实例会跨操作累积状态；每次操作新建实例状态天然干净。`Reflections` 扫描成本（每实例构造一次）实测约几十毫秒级，可接受。配合 D1 的 executor 重建，同一 JVM 内可无限次操作。

### D5. 进度获取走 View 回调，不注册 GUI 监听器

core 的监听器由反射扫描 `core.event.listener` 包自动注册，GUI 监听器若放入会被终端模式误触发。GUI 复用 `ViewSchedule` 每秒回调 `GuiDashboardView.draw(ResultContext)` 获取进度（该回调同时天然承担闭锁补 countDown）。GUI 侧另配一个 `javax.swing.Timer` 做纯视觉的流畅刷新（60fps），两者互不干扰。

### D6. GUI 自做校验，不调用 ConsoleParamValidator.validate()

- 密钥长度/非空/一致性（一致性校验仅加密模式执行，解密模式确认框隐藏，对齐终端语义）、文件后缀、容量、Mac 限制：在 application 层按相同语义实现（复用 `Constants.DEFAULT_PWD_LENGTH` 等常量与 `ConsoleParamValidator.suffixValidate/capacityValidate` 两个无 TTY 依赖的静态方法）。
- 不调 `validate()` 的原因：其 `doubleCheckSecretKey` 在用户从终端启动 GUI 时（TTY 非空）会 `console.readPassword()` 阻塞在终端。GUI 用"密钥 + 确认密钥"双输入框提供等价语义。

### D7. 启动路由在 Starter 内手动剥离 `--gui`

- `Starter.main` 检测 args 含 `--gui`：剥离该参数，剩余参数传入 GUI 作为表单预填（可选）；否则原样走 `CommandLine.execute`。原 picocli 路径零改动（不把 `--gui` 加进 `ConsoleRequest` 注解，避免动 shell 层）。
- GUI 启动：`FlatDarkLaf.setup()` + `SwingUtilities.invokeLater` 打开窗口。

### D8. demo 视觉组件移植与动画映射

- 组件从 test-demo 原样移植（ParticlePanel/GlassCardPanel/GradientButton/TitleBar/淡入淡出/抖动），包名替换为 `com.gxl.encryptdog.gui.interfaces.swing`，**demo 中的 lambda 全部改写为匿名内部类**（遵循 skill 禁 lambda 规范）。
- 动画映射：操作成功 → `triggerBurst()` 粒子聚集爆发（对应 demo 登录成功）；操作失败/校验错误 → `shakeCard()` 卡片抖动（对应 demo 登录失败）；表单 ↔ 进度 ↔ 结果三态切换用 `fadeCard` 淡入淡出。
- 窗口尺寸加大（约 760×760），卡片容纳文件列表 + 表单字段；UiConstants 的粒子/连线/配色常量沿用 demo 数值。

### D9. 依赖与构建

- 新增 `com.formdev:flatlaf:3.7.1`：最低要求 Java 8，Java 15 无需升级。
- `maven-shade-plugin` 2.3 → 3.5.2：flatlaf 为多版本 jar（MRJAR），老 shade 处理 `META-INF/versions` 不可靠。
- slf4j 冲突：flatlaf 传递依赖 slf4j-api 2.x，与 logback 1.2.3（绑定 1.7 API）不兼容。选择在 flatlaf 依赖上 `<exclusion>` 排除 slf4j-api（flatlaf 仅在可选日志场景使用它，加密狗 GUI 不需要），**不升级 logback**，把依赖面变化压到最小。若实测仍有绑定告警，再评估升级 logback 至 1.5.x（备选，不默认执行）。

### D10. logo 资产与位置

- 资产：用户提供的 logo.png（1254×1254 RGBA、透明背景、浅色图形 ≈#F0F0F0、中心镂空）经 `sips -Z 256` 缩为 256×256 PNG，放入 `src/main/resources/logo.png` 随 fat jar 发布；零新依赖，256px 源图为缩放提供充足分辨率。
- 位置：表单头部 40×40 图标 + 英文标题 "EncryptionDog"；标题栏 18×18 小图标（label 显式 18×18 尺寸约束，见头部行几何）。Dock 图标不做。
- 颜色：图形为浅色，与暗色主题（深空渐变 + 毛玻璃）天然适配，无需改色。
- 修正：LogoUtil 加载时按显示尺寸精确缩放（size×size）。原 2 倍缩放（Retina 策略）导致 Icon 尺寸大于 label bounds，JLabel 对超尺寸 Icon 裁切而非缩放，仅显示左上 1/4（表单 80×80 装入 40×40、标题栏 36×36 装入 18×18），见任务 12.9。

### D11. 密码可见性切换组件

- 新建 `PasswordToggleField`（interfaces/swing）：密码框 + 右侧内嵌眼睛图标按钮，点击在 `JPasswordField`（● 掩码）与 `JTextField`（明文）间互换，**互换时保留内容与焦点**。
- 眼睛图标用 Java2D 自绘（睁眼=椭圆+瞳孔，闭眼=椭圆+斜线），颜色取 `TEXT_SECONDARY`——不引入图标资源，任意缩放清晰。
- 应用于密钥与确认密钥两个输入框；英文 tooltip "Show password" / "Hide password"；提交表单仍以 `char[]` 读取，安全语义与终端一致（spec「密码可见性切换」）。

## 参数传递链路（GUI 模式的参数来源与去向）

`ConsoleRequest` 是普通 POJO（@Data，setter 齐全），picocli 注解只是绑定层元数据，仅 `CommandLine.execute` 解析时生效。GUI 模式不经过 picocli，参数来源是表单控件，去向与终端模式完全同构：

```text
终端模式:  CLI args ──picocli解析──▶ ConsoleRequest ─▶ EncryptDogConsole.run() ─▶ 解析/组装/执行
                                        ▲
GUI 模式:  表单控件 ─▶ EncryptFormDTO ─┘ ACL 用 setter 手工填充（注解不参与），下游链路与终端完全一致
```

| 终端参数 | GUI 控件 | ACL 填充（setter） | 核心读取点 |
|---|---|---|---|
| `-s` 源文件/通配符 | 文件选择器（多选） | `setSourceFiles(List)` | `FileNameParser.parse()`（通配符/目录递归/后缀过滤/容量校验） |
| `-k` 密钥 | PasswordToggleField ×2（默认掩码，眼睛图标可切换明文，等价 picocli `interactive=true` 的交互输密） | `setSecretKey(char[])` | `OperationVO.setSecretKey()`（`-o` 时经 `SecretkeyWorker` 替换为随机密钥） |
| `-a` 算法 | 下拉框（默认选中 AES，与字段缺省值一致） | `EncryptTypeEnum.check(name)` 校验后 `setEncryptAlgorithm()` | `EncryptProxy.getChannel()` 选择算法策略 |
| `-e` 加/解密 | 模式切换 | `setEncrypt(boolean)` | `getChannel()`、`OperationVO.setTargetFile()` 的 `.dog` 后缀规则 |
| `-d` 删除源文件 | 复选框 + 确认框 | `setDelete(boolean)` | 加/解密器执行后删除源文件 |
| `-o` 仅本机 | 复选框 | `setOnlyLocal(boolean)` | `OperationVO.setSecretKey()` 判定走 `SecretkeyWorker` |
| `-t` 目标目录 | 目录选择器 | `setTargetPath()` | `OperationVO.setTargetFile()` 组装目标路径 |

契约要点：

- `EncryptProxy.invoke()` 内部直接读 `context.getConsoleRequest().isEncrypt()` / `getEncryptAlgorithm()`，因此 `OperationContext` 携带的必须是**填充完整的** `ConsoleRequest`——ACL 是保证该契约的唯一位置（core 类型不出 ACL，见 D2）。
- `--gui` 后跟随的剩余参数（D7）预填表单时，可复用 picocli 将参数解析进一个 `ConsoleRequest` 再映射为 `EncryptFormDTO`，注解在 GUI 路径下依然可用。
- GUI 表单控件的"可选项集合"与终端一致：算法下拉只有 AES/3DES/XOR 三值，模式只有加密/解密两态，避免产生终端不可能出现的入参组合。

## 界面布局与状态流转（参数的呈现方式）

所有参数呈现在居中的毛玻璃卡片上，窗口约 760×760（无边框圆角 + 星空粒子背景，结构沿用 demo：JLayeredPane 底层粒子 / PALETTE 层卡片）。

**居中规则**：卡片按**整窗中心**对齐（含标题栏的画布），`CARD_Y = (内容区高714 - 卡片高600)/2 - 标题栏高46/2 = 34`。修正前 CARD_Y=57 按内容区居中，标题栏 46px 造成视觉下沉 23px（上边距 103 / 下边距 57），修正后上下边距均为 80px。

**文案与符号规范**：界面文案全英文、无 emoji 与装饰符号、状态仅以颜色区分，见 spec「界面文案与符号」「密码可见性切换」。

**头部行几何（表单头部）**：logo/标题/版本号三件套在头部行内垂直居中对齐——logo y12..52（40×40）、标题 y16..48、版本号 y30..48；模式按钮 y64..100，与头部行底保持 12px 间距（修正前图标底 56 与按钮顶 58 仅 2px 间距，视觉重叠）。下方各区域重排：文件区标签 y120、列表 y112（高 80）、选择按钮 y200；密钥 y238、确认密钥 y286、算法 y334、目标目录 y386、选项 y436/466、错误提示 y498、主按钮 y520（底部留白 36px）。**窗口标题栏**：BoxLayout(X_AXIS) 使内容垂直居中（JLabel alignmentY=0.5），图标 label 显式 18×18 尺寸约束。

卡片内表单自上而下：

1. Logo 与版本号
2. **模式切换**：`Encrypt / Decrypt` 二态切换（对应 `-e`），选中态高亮，同时驱动主按钮文字（Encrypt/Decrypt）与确认密钥框显隐
3. **源文件区**（对应 `-s`）：只读滚动列表展示已选文件（支持移除单项）+ `+ Select files`（JFileChooser 多选）与 `+ Select directory`（递归解析，等价终端传目录）两个入口按钮
4. **密钥区**（对应 `-k`）：两个 JPasswordField（● 掩码）。确认密钥框仅加密模式显示，对齐终端"解密不二次确认"语义（`ConsoleParamValidator.doubleCheckSecretKey` 仅加密时校验）
5. **算法下拉**（对应 `-a`）：AES（缺省选中，与 ConsoleRequest 字段缺省值一致）/ 3DES / XOR 三值
6. **目标目录**（对应 `-t`）：文本框 + 浏览按钮，留空 = 与源文件同目录（终端缺省语义）
7. **选项**（对应 `-d` / `-o`）：删除源文件复选框（提交时二次确认，等价终端第二次 Y/N）、仅限本机复选框
8. **主按钮**：渐变按钮提交

### 页面效果图（ASCII 线框图，配色参见 UiConstants；文案全英文、无 emoji 与装饰符号）

**图1 · 主窗口表单页（加密模式）**

```text
┌──────────────────────────────────────────────────────────────┐
│ [logo 18×18] EncryptionDog                          [ ─ ]  [ ✕ ]  │ ← custom title bar #10102A
├──────────────────────────────────────────────────────────────┤
│                                                              │
│   ·   ·───·        ·       (starry particles + links)       │ ← gradient #0B0B1E→#23104A
│        ·      ·───·      ·      ·       ·                    │
│                                                              │
│    ╭──────────────────────────────────────────────╮          │
│    │   glass card (radius 24 / translucent white) │          │
│    │                                              │          │
│    │    [logo 40×40] EncryptionDog     v2.0.3-RELEASE │          │
│    │                                              │          │
│    │   Mode     [ Encrypt ]  [ Decrypt ] ← toggle │          │
│    │                                              │          │
│    │   Source   ┌───────────────────────────┐     │          │
│    │   files    │ meeting.docx              │     │          │
│    │           │ finance.xlsx               │     │ ← scroll  │
│    │           └───────────────────────────┘     │          │
│    │           [+ Select files]  [+ Select dir]  │          │
│    │           [Remove selected]                 │          │
│    │                                              │          │
│    │   Secret   ┌───────────────────────────┐     │          │
│    │   key      │ ●●●●●●●●●●            ◉  │     │ ← eye    │
│    │           └───────────────────────────┘     │   toggle │
│    │   Confirm  ┌───────────────────────────┐     │ ← encrypt│
│    │   key      │ ●●●●●●●●●●            ◉  │     │   only   │
│    │           └───────────────────────────┘     │          │
│    │                                              │          │
│    │   Algorithm [ AES ▾ ]                        │          │
│    │   Target    [ same as source      ][Browse]  │          │
│    │                                              │          │
│    │   ☐ Delete source files after operation      │          │
│    │   ☐ Local machine only (-o highest security) │          │
│    │                                              │          │
│    │         ┌─────────────────────┐              │          │
│    │         │      Encrypt        │              │ ← gradient│
│    │         │  #6C63FF → #8E7BFF   │              │   button │
│    │         └─────────────────────┘              │          │
│    ╰──────────────────────────────────────────────╯          │
│                                                              │
│  ! error area: red text #FF5C6C + card shake (shakeCard)     │
└──────────────────────────────────────────────────────────────┘
```

**图2 · 表单页解密模式差异**

```text
   Mode     [ Encrypt ]  [ Decrypt ]     (Decrypt selected)

   Secret key field only — no confirm field
   (aligns with terminal: no double-check on decrypt)

   · Source list accepts .dog files only (suffix filter, FileNameParser)
   · Button text: Decrypt
```

**图3 · 确认弹窗（两段，等价终端两次 Y/N）**

```text
① File list confirmation (equivalent to terminal "Please confirm whether it is these files [Y/N]")
        ╭────────────────────────────────────────╮
        │  Confirm files to process (2)          │
        │                                        │
        │  ┌────────────────────────────────┐    │
        │  │ 1. meeting.docx      12.4MB    │    │
        │  │ 2. finance.xlsx       8.1MB    │    │
        │  └────────────────────────────────┘    │
        │                                        │
        │  After encryption:                      │
        │    · meeting.docx.dog                  │
        │    · finance.xlsx.dog                  │
        │                                        │
        │      [ Confirm ]      [ Cancel ]       │
        ╰────────────────────────────────────────╯
  (Cancel = non-Y in terminal → abort, back to form)

② Delete-source confirmation (only when -d checked, equivalent to terminal second Y/N)
        ╭────────────────────────────────────────╮
        │  Delete these source files after       │
        │  the operation?                        │
        │                                        │
        │  ┌────────────────────────────────┐    │
        │  │ · meeting.docx                 │    │
        │  │ · finance.xlsx                 │    │
        │  └────────────────────────────────┘    │
        │                                        │
        │   [ Confirm delete ] [ Keep files ]    │
        ╰────────────────────────────────────────╯
  (Keep files = non-Y in terminal → setDelete(false), continue)
```

**图4 · 进度页（卡片内切换，fadeCard 淡入）**

```text
      ╭──────────────────────────────────────────────╮
      │                                              │
      │   Encrypting…    AES · 2 files               │
      │                                              │
      │   meeting.docx                               │
      │   ████████████████░░░░░░░░  82%   12.4MB     │ ← bar #6C63FF
      │   finance.xlsx                               │
      │   ████████░░░░░░░░░░░░░░░░  41%    8.1MB     │
      │                                              │
      │   Elapsed 00:03 · Remaining 00:05            │
      │                                              │
      │   (refreshed every second via View callback, │
      │    window stays interactive)                 │
      ╰──────────────────────────────────────────────╯
```

**图5 · 结果页（成功 / 部分失败，状态仅用颜色区分）**

```text
All success (title green):
      ╭──────────────────────────────────────────────╮
      │                                              │
      │   Operation completed                        │
      │   Success rate 100% · Failure rate 0%        │
      │   Time 00:08                                 │
      │                                              │
      │   ┌────────────────────────────────┐         │
      │   │ meeting.docx → .dog    (green) │         │
      │   │ finance.xlsx → .dog    (green) │         │
      │   └────────────────────────────────┘         │
      │                                              │
      │   [ Again ]        [ Exit ]                  │
      ╰──────────────────────────────────────────────╯
      background particles gather → burst (triggerBurst) + finish.wav

Partial failure (failed rows red, no burst):
      ╭──────────────────────────────────────────────╮
      │   Operation completed (partial failure)      │
      │   Success rate 50% · Failure rate 50%        │
      │   Time 00:08                                 │
      │                                              │
      │   ┌────────────────────────────────┐         │
      │   │ meeting.docx → .dog    (green) │         │
      │   │ finance.xlsx          (red)    │ ← #FF5C6C
      │   └────────────────────────────────┘         │
      │   [ Again ]        [ Exit ]                  │
      ╰──────────────────────────────────────────────╯
```

状态流转：`表单 → 确认弹窗（文件清单 + 确认/取消）→ 进度 → 结果`，表单/进度/结果三态共用同一张卡片，用 fadeCard 淡入淡出切换（复用 demo 登录页 ↔ 欢迎页的切换模式）：

```text
[表单] ─点击开始─▶ [确认弹窗] ─确认─▶ [进度] ─完成─▶ [结果]
   ▲                文件清单+确认/取消      ▲              │
   └─────────────── 取消/失败 ──────────────┘        [再来一次]按钮
```

- 进度面孔：卡片内逐文件进度条（数据来自 `OperationProgressDTO`，每秒经 View 回调刷新）
- 结果面孔：摘要（总数/成功/失败/成功率/失败率/耗时）+ 逐文件结果 + [Again]/[Exit]，完成时粒子背景触发聚集爆发（triggerBurst）
- 失败：提示区红字错误信息 + 卡片抖动（shakeCard），表单恢复可编辑

## Risks / Trade-offs

- [核心三处微调引入终端回归] → 微调均为"缺省值等价"设计；apply 后按 spec 的"终端模式回归"场景全量回归（三种算法 × 加/解密 × `-d`/`-o`/`-t`/通配符/目录递归）。
- [TTY 检测陷阱（终端里启动 GUI）] → 模式由 `--gui` 参数显式决定（D7），渲染是否进终端由 `view.isConsoleRequired()` 决定（D1），两处与 `System.console()` 解耦。
- [EDT 阻塞导致界面假死] → `invoke()` 必须在后台工作线程执行，Swing 更新一律 `invokeLater`；`GuiDashboardView.draw` 在调度线程回调，内部只提取数据、不做 Swing 操作。
- [每操作一次 Reflections 扫描] → 实测成本低、换取状态隔离（D4）；若后续觉得慢可在 ACL 内缓存策略实例（不改变本次设计）。
- [ACL 内 glue 与终端 buildOperationVO 逻辑漂移] → 两处语义锚定同一批模型方法（`OperationVO.setTargetFile` 等），且"终端模式回归"场景覆盖产物一致性；未来若核心模型变化，ACL 是唯一修改点。
- [shade/slf4j 构建风险] → D9 的排除方案先在本地 `mvn package` 验证，产物用 `java -jar` 双模式冒烟。

## Migration Plan

- 纯增量变更，无数据迁移、无格式升级。发布方式与现有 2.0.3-RELEASE 一致（shade fat jar + alias）。
- 回滚 = revert 本 change 的提交；终端模式不受影响，GUI 代码独立成包可整体摘除。

## Open Questions

（无。影响 specs 或任务拆分的未知项均已在本设计中决策。）
