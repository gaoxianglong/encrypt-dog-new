## Why

执行页进度条现为简单的双色渐变条,视觉平淡。用户基于效果图自研了"Effort Ultracode"风格进度条(见 `/Users/johngao/Desktop/test-swing/EffortUI.java`):四色马赛克渐变(乳白→灰→淡紫→紫)+ 每格确定性相位明灭脉冲 + 圆角深色轨道 + 进度填充,并希望直接用该代码替换现有效果;为让进度条足够"震撼",允许拉大窗口长宽。

## What Changes

- 执行页进度条 SHALL 替换为 Effort 风格渲染:四色渐变马赛克方格(乳白→灰→淡紫→紫,6px 方格、1px 缝隙、2 行)、每格按确定性相位持续明灭脉冲、圆角深色轨道固定 253px 宽 × 12px 高且自左端起(照搬示例 TRACK_END−SLIDER_X=253、SLIDER_H=12 数值)、填充随真实进度增长(未产生进度时无填充)。现有紫色渐变 `GradientBar` SHALL 被新组件替代。
- 进度条右侧百分比文本保持不变(既有已验收要求);失败行停留失败时刻进度、未产生进度显示 0% 等行为 SHALL 不变。
- 执行页窗口 SHALL 放大以承载更大进度条(宽窗口 1200×800 → 1400×880);执行页行高 40 → 46、进度条厚度 10 → 12(与示例 SLIDER_H 一致、方格 6px)、进度列宽比进一步加大(215 → 300);主页(表单页)窗口与卡片尺寸 SHALL 保持原样不变;表单页卡片与执行页宽卡片随常量自动居中/变宽,其余布局按比例自适应。
- 移植范围仅为进度条渲染效果本身;演示代码中的场景装饰(标题文字、像素小宠物、胶囊按钮、Faster/Smarter 标签、轨道缩短 1/3 构图)不属于执行页,不移植。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 修改「执行表格视图」要求——进度条改为四色马赛克脉冲渲染;修改「主题主色系」要求——进度条渲染说明与主色系关系更新。

## Impact

- `gui/interfaces/swing/ProgressPanel.java`: 以新组件(MosaicBar)替换 `GradientBar`;新增共享脉冲 Timer(33ms,begin 启动/返回重置停止,仅重绘行区);行高、进度列比例与进度条厚度调整。
- `gui/interfaces/swing/constant/UiConstants.java`: 窗口宽高、卡片宽高、新增马赛克四色与轨道色常量。
- `gui/interfaces/swing/EncryptDogFrame.java`: 仅随常量自动适配(卡片居中计算不变),无逻辑改动预期。
- 不涉及 core、终端模式。
