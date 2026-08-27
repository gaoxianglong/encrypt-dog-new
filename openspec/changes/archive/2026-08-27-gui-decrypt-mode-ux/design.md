## Context

`EncryptFormPanel` 采用 `setLayout(null)` 绝对坐标布局,所有控件在构造方法中一次性 `setBounds`。当前 `switchMode(boolean)` 只驱动确认密钥行显隐、主按钮文字与 XOR 提示,标题/副标题是构造方法局部变量,无法随模式更新。

现状关键坐标(单位 px,`INPUT_HEIGHT = 40`):

```
Secret key   字段 y=252 (底 292)
Confirm key  字段 y=298,标签 y=306          ← 解密时 setVisible(false)
Algorithm    字段 y=344,标签 y=352,XOR 提示 y=344
Target dir   字段 y=390,标签 y=398
复选框        y=436 / y=466
错误提示      y=498
主按钮        y=522 (高 44)
版本号        y=578(右下角,不动)
```

相邻字段行距恒为 46(252→298→344→390),这是重排偏移量的天然来源。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 模式切换后标题/副标题与当前模式一致,标题块位置不变。
- 解密模式无空洞:确认密钥行隐藏后其下方模块整体上移一个行距,Secret key 与 Algorithm 的间距与加密模式相邻字段间距完全一致(均为 46 行距、字段间 6px 视觉间隙)。
- 加密模式布局与现状逐像素一致;版本号两种模式下均固定右下角。

**Non-Goals:**

- 不改动窗口/卡片尺寸,不引入动画过渡,不重构为布局管理器。
- 不触碰 core、终端模式与 `ProgressPanel`/`ResultPanel`。

## Decisions

### D1: 采用"基准坐标 + 单行偏移"重排(方案 A),而非其他方案

用户从四个候选方案中选定方案 A(动态重排)。其余方案被否原因:

| 方案 | 否决理由 |
|---|---|
| B 原位禁用 | 解密模式常驻一个无用的禁用控件,增加认知噪音 |
| C 提示条占位 | 该行始终占位,解密表单无法更紧凑 |
| D 布局管理器重构 | 改动面过大,像素级精调的视觉有回归风险,与本次修复目标不成比例 |

### D2: 偏移量 = 相邻字段行距 46,不新增魔法数

确认密钥行隐藏后,Secret key 字段(252)与 Algorithm 字段(344)之间正好缺一个行距。定义面板私有常量 `ROW_PITCH = 46`(与现有 `LABEL_WIDTH`、`FIELD_X` 等常量风格一致,不改 `UiConstants`),解密模式偏移 `-ROW_PITCH`。移后 Algorithm 字段 y=298,与加密模式 Secret key→Confirm key 的行距完全一致,视觉节奏自动保持。

### D3: 可移动行的坐标收敛到 `layoutRows(boolean)`,构造时即走同一路径

新增私有方法 `layoutRows(boolean isEncrypt)`,`int off = isEncrypt ? 0 : ROW_PITCH`,集中设置以下控件的 bounds(构造方法中不再单独设置它们的 y):

| 控件 | 基准 y | 解密 y |
|---|---|---|
| Algorithm 标签 / 下拉框 / XOR 提示 | 352 / 344 / 344 | 306 / 298 / 298 |
| Target dir 标签 / 输入组 | 398 / 390 | 352 / 344 |
| 两个选项复选框 | 436 / 466 | 390 / 420 |
| 错误提示 | 498 | 452 |
| 主按钮(仅 y,x 不变) | 522 | 476 |

- 构造方法末尾调用 `layoutRows(true)` 完成可移动行初始化;`switchMode` 末尾调用 `layoutRows(isEncrypt)` 后 `revalidate()` + `repaint()`。
- `targetBox`(`BorderLayout` 包 Browse 按钮的容器)由局部变量提升为字段;`addLabel` 对 Algorithm、Target directory 两个标签返回/保存引用,其余标签不变。
- `prefill()` 已调用 `switchMode(form.isEncrypt())`,自动获得重排,无需额外处理。

选择集中式 `layoutRows` 而非"两套坐标表":单一事实来源,新增字段时只需登记到该方法;若分散在 switchMode 里逐个改 bounds,后续字段增删极易漏改、重现空洞。

### D4: 标题/副标题提升为字段,文案在 `switchMode` 内切换

`taskTitleLabel`、`subtitleLabel` 提升为字段(字号/颜色样式仍在构造时设置一次);`switchMode` 中按模式 `setText`:

- Encrypt: "Encrypt files" / "Protect your files with local encryption"
- Decrypt: "Decrypt files" / "Restore your files with local decryption"

标题块 bounds 不变(满足「头部排版」要求:标题块与模式切换栏位置不受影响)。

### D5: 幂等与重入安全性

`SegmentedToggle.setSelectedIndex` 对同索引直接返回(已确认源码守卫),`switchMode` 内先 `setSelectedIndex` 再驱动的现有模式不会产生监听器重入;`layoutRows` 为绝对 setBounds,重复调用幂等。

## Risks / Trade-offs

- [解密模式底部留白增大:主按钮上移 46px 后与版本号之间空白变多] → 版本号钉在右下角不动,留白集中在按钮下方,属可接受的重心变化;若后续视觉验收不满意,再评估卡片高度压缩(超出本次范围)。
- [未来新增字段漏登记 layoutRows,重现空洞] → 在 `layoutRows` 与确认密钥行字段的 javadoc 中明确约定:「位于确认密钥行下方的任何控件必须登记到 layoutRows」。
- [加密模式视觉回归] → 构造路径统一走 `layoutRows(true)`,基准坐标与现状完全一致;验收时对照加密模式截图逐像素核对。
- [setVisible + setBounds 的刷新时机] → `switchMode` 末尾统一 `revalidate()` + `repaint()`,避免解密模式下残留旧绘制区域。

## Migration Plan

纯 GUI 单文件变更(面板内私有常量/字段/方法),无数据迁移;回滚即恢复原坐标与文案,不影响已加密产物与终端模式。
