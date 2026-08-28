## Context

执行页表格表头由 `ProgressPanel.HeaderBand` 自绘:圆角 12、`DEEP_ACCENT #3A2A66` α150 半透明填充,表头文字 `TEXT_SECONDARY` 11F 加粗居中。用户要求表头背景改用主色系紫,与主题强调组件(按钮/进度条/滚动条)统一。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 表头背景改为主色系半透明紫,与整体主题协调。

**Non-Goals:**

- 不改表头文字颜色/字号/对齐、圆角、高度与任何布局。
- 不改 `DEEP_ACCENT` 常量本身(行条与提示浮层继续使用深紫)。

## Decisions

### D1: HeaderBand 填充色替换(ProgressPanel)

`HeaderBand.paintComponent` 中填充色由 `DEEP_ACCENT` 改为 `UiConstants.MOSAIC_PUR`(主色系四色渐变的紫段 #7A5FA8),alpha 保持 150:比 ACCENT 更深、与马赛克进度条紫段同源;表头文字由 `TEXT_SECONDARY` 改为 `TEXT_PRIMARY`(近白),与主题"紫底浅字"观感一致、对比度可读。圆角/字号/对齐/布局零改动。

备选:ACCENT α150(首轮)→ 验收反馈背景偏浅;ACCENT 提高 alpha → 放弃,仍是浅紫调;MOSAIC_PUR 是主色系内唯一既有深紫,直接复用且与进度条四色渐变同源。

## Risks / Trade-offs

- [表头文字对比度] → 首轮验收反馈灰紫文字在紫底上不清,已改 `TEXT_PRIMARY` 近白文字;深紫底 + 近白字对比度充足,以验收实测确认。
- [改动范围回归] → 仅一处绘制色替换,其他页面与 core 零触碰。

## Migration Plan

纯色值变更,无数据迁移;回滚即恢复 `DEEP_ACCENT` 填充,不影响功能与终端模式。
