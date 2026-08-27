# Tasks: Add Swing GUI

## 1. 构建与依赖

- [x] 1.1 pom.xml 新增 `com.formdev:flatlaf:3.7.1` 依赖并对 slf4j-api 做 exclusion，验证 `mvn dependency:tree` 中 slf4j-api 仍为 1.7.x 且 flatlaf 在依赖树中
- [x] 1.2 pom.xml 升级 maven-shade-plugin 2.3 → 3.5.2，验证 `mvn package` 构建成功且 fat jar 内 flatlaf 的 `META-INF/versions` 结构完整
- [x] 1.3 验证打包产物以终端模式启动冒烟：`java -jar dog-2.0.3-RELEASE.jar -h` 输出帮助信息正常

## 2. 核心三处纯增量微调

- [x] 2.1 `View` 接口新增 `default boolean isConsoleRequired() { return true; }`，验证项目编译通过且 `DashboardView` 无需改动
- [x] 2.2 `ViewSchedule.renderDashboardView` 渲染条件改为 `!Utils.isConsole() && view.isConsoleRequired()` 时跳过，`stop()` 改为 shutdownNow 后重建 executor，验证编译通过
- [x] 2.3 `EncryptProxy` 新增 `setView(View)` setter（缺省仍为 `new DashboardView()`），验证编译通过

## 3. 终端模式全量回归（GUI 开工前必须通过）

- [x] 3.1 终端模式 AES 加密 + 解密各一轮（含 Y/N 确认、Dashboard 每秒刷新、finish.wav），验证产物、交互与改动前一致
- [x] 3.2 终端模式 3DES、XOR 算法各一轮加/解密，验证算法与产物一致
- [x] 3.3 终端模式 `-d` 删除源文件二次确认、`-o` 仅本机、`-t` 目标目录、通配符与目录递归各一轮，验证行为与改动前一致

## 4. GUI 防腐层（infrastructure/acl）

- [x] 4.1 新建 `EncryptCoreFacade`：表单 DTO → `ConsoleRequest`/`OperationVO` 组装（含 D3 的 glue 等价实现），验证单元编译通过且组装的 OperationVO 字段与终端 `buildOperationVO` 语义一致（对照 review）
- [x] 4.2 `EncryptCoreFacade` 实现执行入口：每次操作 `new EncryptProxy()` + `setView` + 后台线程 invoke，并读取最终 `ResultContext` 转 `OperationResultDTO`，验证异常（BaseException）能转换为 GUI 错误信息不向外泄漏 core 类型
- [x] 4.3 实现 `GuiDashboardView implements View`（`isConsoleRequired()` 返回 false，draw 内只提取 ResultContext 数据并经回调推送到 application 层，不做 Swing 操作），验证无 TTY 启动时调度任务正常执行且闭锁正常释放（用临时 main 冒烟：无终端环境 invoke 不阻塞）

## 5. GUI 应用层（application）

- [x] 5.1 新建 DTO：`EncryptFormDTO` / `OperationProgressDTO` / `OperationResultDTO` / 回调接口 `OperationListener`，验证编译通过
- [x] 5.2 新建 `EncryptOperationAppService`：编排"校验 → 解析（复用 `FileNameParser` 与 `ConsoleParamValidator.suffixValidate/capacityValidate`）→ 确认 → 委托 ACL 执行 → 回显"，验证密钥长度/一致性/Mac 限制校验规则与终端语义一致
- [x] 5.3 应用层禁止 import 任何 `com.gxl.encryptdog.core` 类型（除经 ACL 透传的 DTO），验证 `grep -r "encryptdog.core" gui/application` 无结果

## 6. GUI 表现层组件移植（interfaces/swing）

- [x] 6.1 新建 `UiConstants`：沿用 demo 的配色/粒子/动画常量并扩充窗口尺寸（约 760×760）与卡片尺寸，验证编译通过
- [x] 6.2 移植 `ParticlePanel`（星空连线/鼠标微扰/聚集爆发），demo 中 lambda 全部改写为匿名内部类，验证运行 demo 级窗口时粒子动画流畅（肉眼观察）
- [x] 6.3 移植 `GlassCardPanel`、`GradientButton`、`TitleBar`，同样改写 lambda，验证窗口圆角/毛玻璃/标题栏拖拽与关闭正常
- [ ] 6.4 移植淡入淡出（fadeCard）与卡片抖动（shakeCard）动画逻辑到主窗口，验证动画时序正确

## 7. GUI 表单面板

- [x] 7.1 新建 `EncryptFormPanel`：源文件区（选择文件多选 + 选择目录两个入口，JFileChooser）、密钥+确认密钥（JPasswordField，确认框仅加密模式显示）、算法下拉（AES/3DES/XOR）、加/解密模式切换、删除源文件/仅本机复选框、目标目录选择器，验证表单字段与终端参数一一对应
- [ ] 7.2 表单校验错误提示（密钥不一致/长度不足/未选文件等）接入卡片抖动，验证各错误场景提示正确且不提交执行
- [ ] 7.3 文件列表确认对话框（解析后展示清单，等价终端 Y/N；删除源文件时二次确认），验证取消时不执行任何操作

## 8. GUI 进度与结果面板

- [ ] 8.1 新建 `ProgressPanel`：按 `OperationProgressDTO` 逐文件渲染进度条，并展示已用时（GUI 自身计时）与预计剩余（来自 ResultContext 各文件 estimatedTime），验证执行期间界面可交互（窗口可拖动）且进度持续更新
- [ ] 8.2 新建 `ResultPanel`：展示总数/成功/失败/成功率/失败率/耗时/逐文件结果，验证与 `OperationResultDTO` 数据一致
- [ ] 8.3 组装 `EncryptDogFrame`：表单 → 进度 → 结果三态切换（fadeCard 过渡）、操作成功触发 `triggerBurst()`、失败触发 `shakeCard()`，验证整体流程动效符合 demo 观感

## 9. 启动路由

- [x] 9.1 `Starter.main` 剥离 `--gui` 参数并分流：GUI 走 `FlatDarkLaf.setup()` + `invokeLater` 开窗，其余参数原样走 picocli，验证无 `--gui` 时终端行为完全不变
- [x] 9.2 `--gui` 后剩余参数预填表单（源文件/算法等，可选预填），验证带参数启动 GUI 时表单已预填

## 10. 打包与双模式冒烟

- [ ] 10.1 `mvn package` 打包 fat jar，验证 `java -jar dog.jar --gui`（终端启动与双击启动两种场景）窗口正常打开、完成一轮加密+解密全流程（含进度、爆发动画、提示音）
- [x] 10.2 同一 GUI 会话内连续执行第二次操作，验证无 RejectedExecutionException、状态不残留
- [x] 10.3 终端模式最终回归一轮（复用 3.x 场景），验证 GUI 功能合入后终端行为不变

## 11. 文档

- [x] 11.1 README 增加 GUI 模式使用说明（`--gui` 启动方式与界面操作说明），验证说明与实际行为一致

## 12. GUI 打磨（英文文案/去符号/居中/logo/密码可见性）

- [x] 12.1 全部 GUI 文案英文化（表单/进度/结果/弹窗/标题栏/tooltip），验证界面无中文残留
- [x] 12.2 移除 emoji 与 ✓✗✦ 等装饰符号，成功/失败仅用颜色区分（绿/红），验证各状态颜色正确
- [x] 12.3 CARD_Y 57→34 居中修正（对齐整窗中心，上下边距 80/80），验证视觉居中
- [x] 12.4 README GUI 节同步为英文/去符号/新布局/logo 说明（design.md 线框图已随规划同步完成）
- [x] 12.5 集成盾牌 logo：SVG 经 qlmanage 转 256px PNG 放入 src/main/resources/logo.png，表单头部 40×40 图标 + 英文标题、标题栏 18×18 图标，验证暗色背景下清晰显示
- [x] 12.6 新建 PasswordToggleField（Java2D 自绘睁眼/闭眼图标，掩码↔明文切换保留内容与焦点），替换密钥与确认密钥两个密码框，验证切换不丢内容且提交仍为 char[]

- [x] 12.7 标题栏与表单头部垂直居中修正：窗口标题栏改 BoxLayout(X_AXIS) 显式垂直居中、图标 label 显式 18×18；表单头部 logo y16→12、标题 y20→16、版本号 y34→30 三件套居中对齐，模式按钮 y58→64 拉开 12px 间距，下方区域重排（文件区标签 116→120、列表 108→112 且高 88→80、选择按钮 206→200、密钥 246→238、确认密钥 294→286、算法 342→334、目标目录 394→386、选项 444→436/474→466、错误提示 504→498、主按钮 528→520，底部留白 36px），验证头部与模式区无视觉重叠
- [x] 12.8 替换新 logo：sips 缩 1254×1254 → 256×256 覆盖 src/main/resources/logo.png，重新打包，验证表单头部 40×40 与标题栏 18×18 显示正常（显示验证由 12.9 承接修复）

- [x] 12.9 修复 logo 裁切：LogoUtil.loadLogo 的 2 倍缩放导致 Icon 尺寸大于 label bounds（表单 80×80 装入 40×40、标题栏 36×36 装入 18×18），JLabel 对超尺寸 Icon 是裁切而非缩放，仅显示左上 1/4；改为按显示尺寸精确缩放（size×size，256px 源图缩至 40/18px 仍足够清晰），验证表单头部与标题栏 logo 完整显示
