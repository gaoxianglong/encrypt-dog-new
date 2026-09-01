# 执行页状态列胶囊徽章渲染

## Why

执行页 State 列目前以纯文本（Waiting/Running/Finished）+ 文字变色区分状态，视觉存在感弱，与执行页整体的自绘玻璃质感（半透明圆角行条、马赛克进度条、统计小窗）不协调。用户希望状态渲染成按钮般的徽章效果，提升美观度与状态辨识度。

## What Changes

- State 列单元格由纯文本改为**半透明圆角胶囊徽章**（soft chip）：圆角矩形低透明度底色 + 1px 同色描边 + 状态色文字，沿用主题玻璃卡片的半透明视觉语言。
- 三态配色为主题同源阶梯：WAITING 灰色（TEXT_SECONDARY）、RUNNING 紫色（ACCENT_BRIGHT）、FINISHED 薰衣草白（TEXT_PRIMARY）；绿色语义仅保留给 Result 列对勾图标（视觉验收反馈：绿色胶囊在紫灰主题中突兀）。
- 三态徽章均**静态渲染**（视觉验收反馈：呼吸动画取消）。
- State 列宽比 75 → 92（胶囊内边距 + 文字约需 78px，现状 ~84px 偏挤），Progress 列 300 → 283 补偿（进度条固定 253px，该列有富余），列宽比总和不变。
- 保持三态，**不引入 FAILED 徽章**：失败行的状态仍显示 Finished，失败语义继续由 Result 列红色错误图标（悬停展示原因）承担，与既有「状态颜色区分」场景一致。
- 状态展示文本保持现状（首字母大写其余小写：Waiting/Running/Finished），仅改渲染形式，不改文案语义。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

- `swing-gui`: 「执行表格视图」需求中 State 列的状态渲染形式由纯文字改为半透明圆角胶囊徽章（三态配色 + RUNNING 呼吸动画），并调整 State/Progress 列宽比；其余执行页行为不变。

## Impact

- 受影响代码：`gui/interfaces/swing/ProgressPanel.java`（State 列单元格组件、列宽比常量、行构建逻辑）；可能新增一个自绘徽章组件类（与既有 RowStrip/MosaicBar 同风格的自绘 JComponent）。
- 复用既有资源：状态三色（TEXT_SECONDARY/ACCENT_BRIGHT/SUCCESS_GREEN）均已存在于 `UiConstants`，33ms 脉冲时钟已存在，无需新增基础设施。
- 无 API 变更、无核心（终端模式）变更、无依赖变更。
