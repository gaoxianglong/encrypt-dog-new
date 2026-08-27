## Context

`ThemedConfirmDialog` 现状:无边框圆角模态 JDialog,内容面板自绘 `BG_TOP→BG_BOTTOM` 渐变 roundRect,`setVisible` 后应用 `setShape(RoundRectangle2D)` 裁剪(与主窗口同模式)。上线后渐变颜色正确,但四角露出系统默认灰色直角——原因:内容面板未 `setOpaque(false)`(JPanel 默认不透明、默认底色),窗口自身背景也未置透明,圆角外的矩形区域以系统默认灰色呈现。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 弹窗四角为透明圆角,无灰色直角;渐变与描边完整贴合圆角。
- 修改仅限 `ThemedConfirmDialog`,不动主窗口与其他组件。

**Non-Goals:**

- 不改动弹窗文案、按钮与交互语义。
- 不调整主窗口 EncryptDogFrame 的圆角实现。

## Decisions

### D1: 窗口级透明:dialog.setBackground(全透明)

构造中调用 `setBackground(new Color(0, 0, 0, 0))`,消除窗口默认灰色背景——即使圆角裁剪在平台实现上有偏差,角落区域也不会以灰色呈现。

### D2: 内容层透明:面板与根面板 setOpaque(false)

内容面板自绘渐变时不调用 `super.paintComponent`,将其 `setOpaque(false)` 后,面板自身不再涂默认底色,圆角外区域透出 D1 的透明窗口背景;同时 `getRootPane().setOpaque(false)` 防止根面板兜底涂色。

### D3: 保留 setShape 圆角裁剪(双保险)

保留 `setVisible` 后应用 `setShape` 的现有模式:透明底负责"看不见灰色",圆角裁剪负责"角落不参与点击命中",两者独立生效,任一失效另一端兜底。

替代方案:仅面板 `setOpaque(false)`(否决——窗口级灰色背景仍可能露出);去掉 `setShape` 只靠透明底(否决——窗口命中区仍为矩形,角落可穿透点击)。

## Risks / Trade-offs

- [透明窗口的平台支持差异] → 目标平台为 macOS,Java AWT 支持 setBackground 透明与 setShape;若验收仍见异常,按 D1/D2/D3 分层排查,回退空间充足。
- [视觉验收主观] → 验收时截图对照四角与主窗口圆角的一致性。

## Migration Plan

纯 GUI 单文件变更,无数据迁移;回滚即恢复灰色角落现状,不影响功能行为。
