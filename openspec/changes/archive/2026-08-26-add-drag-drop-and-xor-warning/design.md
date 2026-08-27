# Design: Drag-and-Drop and XOR Warning

## Context

`EncryptFormPanel` 当前源文件区为"标签 + JList/滚动面板 + 三个按钮"散置布局，模式切换为两个独立按钮。本次将模式切换、拖拽框、操作按钮重组为一个完整模块（宽 420 = FIELD_WIDTH，x = FIELD_X = 134，与下方密钥/算法/目录输入框同宽同边缘），模块整体替换现有 y64..230 区域，下方各区域（密钥 y238 起）几何不变。界面规范：全英文文案、无装饰符号、新代码禁用 lambda（匿名内部类）、动画统一 javax.swing.Timer。本 change 无新依赖，全部基于 Swing 原生能力。

## Goals / Non-Goals

**Goals:**

- 模块化重组：等宽分段选择器 + 深色虚线拖拽框（空态图标提示 ↔ 框内列表）+ 左选择/右删除按钮行，宽度与输入框一致、元素间距与边缘对齐统一
- 拖拽添加与按钮选择语义完全一致（路径入列表、去重、目录递归与后缀过滤在解析阶段）
- 三层动画（悬停脉冲/放下涟漪/行淡入）使用主题色 ACCENT 与既有动画语言
- XOR 警告仅加密模式展示，低调不阻断

**Non-Goals:**

- 不做列表内拖拽排序、不做拖出（列表只读）
- 不新增配色（深色底用半透明黑、警告用既有 `TEXT_SECONDARY`）
- 不改动解析/执行核心链路

## Decisions

### D1. 源文件模块结构（EncryptFormPanel 内，替换旧布局）

```text
y=64  分段选择器: [ Encrypt | Decrypt ]   (y64..100, h36, 各210px, 选中ACCENT填充白字, 未选透明)
y=104 拖拽框(深色底rgba(0,0,0,60) + 虚线边框 + 圆角)   (y104..216, h112, 用户反馈92过窄后加高)
        空态: fileEmpty图标居中(48px) + "Drag files or folders here"  ← TEXT_SECONDARY
        有文件: JScrollPane承载的滚动列表(AS_NEEDED细滚动条, FlatLaf主题样式,
               viewport透明无边框, 委托FlatLaf渲染+行淡入alpha)
              —— 修正: 模块重组时JList丢失了原JScrollPane包裹导致无滚动条(实测反馈)
y=220 按钮行: [+ Select files][+ Select directory]  ...  [Remove selected]  (y220..246, h26)
        ← 左对齐                                           右对齐 →
       （下方区域整体 +14: 密钥 252..292 / 确认密钥 298..338 / 算法 344..384 /
        目标目录 390..430 / 选项 436..492 / 错误提示 498..520 / 主按钮 522..566, 底部留白 34px；
        四框间隙统一 6px、标签统一 field+8 垂直居中（实测反馈：原 4/6/12/10 间隙不均）；
        宽度协调方案B（实测反馈：四框长度参差）：密钥/确认密钥/目标目录三框 420 全宽——
        目标目录的 Browse 按钮内嵌字段右端(98px, 与密码框眼睛按钮同模式)；算法行保持
        "短下拉180 + 右侧警告文字"的控件+注释设计，四行右缘统一对齐 554）
       （原 "Mode" 与 "Source files" 两个标签取消,拖拽框自解释）
```

- 分段选择器：自绘 JPanel + 两个鼠标区域（复用现有 `switchMode` 状态机），选中段 ACCENT 填充、圆角与整体风格一致；不再使用两个独立 JButton。
- 拖拽框：自绘组件 `DropFilePanel`——paintComponent 画深色圆角底 + 虚线描边（BasicStroke dash）；空态绘制 fileEmpty 图标（居中）+ 提示文字；有文件时切换为列表视图（内部持有 JList，空态视图与列表视图按 `fileListModel.isEmpty()` 互换）。
- 按钮行：`+ Select files`、`+ Select directory` 左对齐排列，`Remove selected` 右对齐（弹簧式间距），与模块左右边缘对齐。
- 原 "Source files" 标签取消（拖拽框自解释）。

### D2. 拖拽接入：自定义 TransferHandler 挂在拖拽框/列表上

- `canImport`：仅接受 `DataFlavor.javaFileListFlavor`；`importData`：取出 `List<File>`，文件与目录一律以路径加入 `fileListModel`（复用现有 `addFilePath` 去重；目录递归解析与后缀过滤留待提交时由 FileNameParser 统一处理，与 `+ Select directory` 语义一致）。
- 覆盖 JList 默认 handler（避免默认的文本拖出/重排行为）；不设 setDropMode。
- 悬停状态通过 `dragEnter`/`dragExit`/`dragOver` 回调驱动动画开关。
- **拖拽失效修复（实测反馈）**：① accept 必须显式 `DnDConstants.ACTION_COPY`——macOS Finder 在 dragEnter 阶段报告的 drop action 常为 ACTION_NONE，用它 acceptDrag 会令系统判定不接受导致拖拽整体失效；② DropTarget 必须按组件分别创建与绑定（面板一个、JList 一个），原实现 `new DropTarget(this, listener)` 硬编码面板引用又绑定到列表，组件引用错位。

### D3. 三层主题动画（Timer 驱动，匿名 ActionListener）

1. **悬停呼吸脉冲**：`dragEnter` 时启动约 800ms 周期 Timer，驱动拖拽框虚线边框颜色在 `ACCENT` 与 `TEXT_SECONDARY` 间正弦振荡；`dragExit`/成功 import 后停止复位。
2. **放下涟漪**：drop 成功后触发一次约 350ms 渐变扫过——GradientPaint 起点 `ACCENT`（alpha 衰减）沿水平方向平移一次后隐藏（在 DropFilePanel 的 paintComponent 内绘制，避免额外 overlay 层）。
3. **行淡入**：自定义 `ListCellRenderer` 委托 FlatLaf 默认渲染后按行索引查 `Map<Integer, Float>` 淡入进度（0→1），新加入的行以 40ms 步进依次推进（一个 Timer 统一驱动，末行完成后停止）。列表选中、移除行为不受影响。

### D4. fileEmpty 图标资产

- `/Users/johngao/Downloads/24gl-fileEmpty.png`（200×200 RGBA、透明背景、浅灰 #E0E0E0，与 logo 同色系）经 `sips -Z 128` 缩为 128×128 放入 `src/main/resources/file-empty.png` 随 jar 发布。
- `LogoUtil` 扩展为通用资源加载（`loadImage(resource, size)`，按显示尺寸精确缩放——吸取 12.9 教训，Icon 尺寸与显示尺寸严格一致），`loadLogo` 保持为便捷方法。

### D5. XOR 安全提示

- `EncryptFormPanel` 新增 `xorWarningLabel`（`TEXT_SECONDARY`、SMALL_FONT），bounds（322, 算法行 y, 232, 40）与下拉框同高 + 垂直居中对齐（`JLabel.CENTER`），文案 "Use with caution"（实测反馈：原长句过长且未垂直对齐）。
- 显隐统一收敛到 `updateXorWarning()`：`visible = isEncryptMode() && "XOR".equals(algorithmCombo.getSelectedItem())`。
- 三处联动调用：分段选择器切换（switchMode）、算法下拉的匿名 `ItemListener`、`prefill()`（预填后经 switchMode 触发）。

### D6. 标题栏 logo 与标题比例

- 窗口标题栏：logo 显示尺寸 18→22（`LOGO_TITLE_SIZE`），标题字体 SMALL(12)→22 加粗——logo 与 "EncryptionDog" 同高平行（实测反馈：原比例字体过小不协调）。

## Risks / Trade-offs

- [模块重排挤压下方区域] → 拖拽框高度与各区域间距在 600px 卡片内统一核算（见 D1 坐标），主按钮保持底部留白 ≥36px
- [JList 渲染器覆盖影响选中样式] → 行淡入 renderer 委托 FlatLaf 默认渲染后仅叠加 alpha
- [拖拽悬停状态残留] → dragExit/drop 完成都执行动画复位
- [空态↔列表切换丢失选择状态] → 切换仅更换视图组件，JList 与 model 常驻，选中索引保留
- [回归面] → 表单既有交互（按钮选择/移除/预填/模式切换）与终端模式不受影响，apply 后按任务 3.1 冒烟

## Migration Plan

纯增量，无数据/格式变化；回滚 = revert 本 change 提交。

## Open Questions

（无）
