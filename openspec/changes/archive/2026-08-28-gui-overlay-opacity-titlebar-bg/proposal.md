## Why

窗口顶部标题栏区域因 TitleBar 透明且未绘制背景，露出 JFrame 默认灰色底，与紫色渐变主体形成明显拼缝，视觉上像两个系统拼接。

## What Changes

- 标题栏 SHALL 绘制主题背景色（背景渐变顶部色 `BG_TOP` #0B0B1E），与下方粒子层渐变的顶部颜色完全一致，拼缝消失；标题栏内容（logo、品牌名称、最小化/关闭按钮、拖拽交互）与既有行为不变。
- 删除从未被引用的 `TITLE_BAR_BG` 常量（其色值 #10102A 与渐变顶部色不一致，直接复用会造成细微拼缝）。
- 蒙层不透明度曾在实施中试调（`CARD_FILL` alpha 20 → 64）供目视验收，用户否决后已回滚至原值 20，不属于本变更的交付范围（记录见 design.md）。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 新增「标题栏背景色统一」要求——标题栏背景与主题背景渐变顶部色一致，交界处无拼缝、无灰色系统底色，标题栏内容与交互不变。

## Impact

- `gui/interfaces/swing/TitleBar.java`: 增加背景绘制（填充 `BG_TOP`），组件结构、窗口按钮与拖拽逻辑不动。
- `gui/interfaces/swing/constant/UiConstants.java`: 删除未使用的 `TITLE_BAR_BG` 常量；`CARD_FILL` 保持原值（试调已回滚）。
- 主页（EncryptFormPanel）与执行页（ProgressPanel）位于 GlassCardPanel 之内，不受本变更影响。
- 不涉及 core、终端模式；不涉及 ThemedConfirmDialog（已自绘主题渐变背景，无灰色底问题）。
