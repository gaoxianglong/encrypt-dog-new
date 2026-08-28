## Context

动机见 proposal.md「Why」。现状与约束：

- **标题栏**：主窗口 `setUndecorated(true)` + 自绘 TitleBar；TitleBar `setOpaque(false)` 且仅绘制窗口按钮悬停圆底，其 46px 高度区域露出 JFrame 内容面板默认灰色底。粒子背景渐变 `BG_TOP(#0B0B1E) → BG_BOTTOM(#2B1A52)` 仅覆盖内容区（y≥46），渐变首行即 BG_TOP。UiConstants 已有 `TITLE_BAR_BG(#10102A)` 但从未被任何代码引用。
- **窗口形状**：整窗通过 `setShape(RoundRectangle2D)` 裁剪为圆角，标题栏四角随窗口形状裁剪，纯色填充不会越界。
- ThemedConfirmDialog 已自绘紫色渐变且窗口背景全透明，无灰色底问题，不涉及。
- **已回滚尝试记录**：实施初期曾随本变更将蒙层 `CARD_FILL` alpha 20 试调至 64（约 25%）以提升内容可读性，用户目视验收否决（玻璃观感变差），已回滚至 20。蒙层保持原样，不属于本变更范围。

## Goals / Non-Goals

**Goals:**

- 标题栏绘制 BG_TOP 背景，与内容区渐变顶部颜色完全一致，拼缝消失。
- 清理未使用的 TITLE_BAR_BG 常量。

**Non-Goals:**

- 不改蒙层填充色（CARD_FILL）、CARD_BORDER、CARD_SHADOW、圆角与布局参数。
- 不改标题栏内容与交互（logo、品牌名称、最小化/关闭按钮、悬停效果、按住拖拽）。
- 不动 ThemedConfirmDialog、星空粒子参数、终端模式与 core。

## Decisions

### D1: 标题栏绘制 BG_TOP 背景

TitleBar.paintComponent 首行填充 `UiConstants.BG_TOP`（保持 `setOpaque(false)` + 显式填充：不开 opaque，避免 Swing 优化绘制路径变化，子面板 titleBox/buttonBox 透明不受影响）。

- 选 BG_TOP 而非既有 TITLE_BAR_BG(#10102A)：内容区渐变首行即 BG_TOP，只有同色才能做到边界无缝；#10102A 与 #0B0B1E 的细微差异会在 46px 边界留下可感知拼缝，违背本次目标。
- 删除从未引用的 TITLE_BAR_BG 常量（死代码），避免未来双色定义漂移；后续若需标题栏独立配色，再新增派生常量。
- 备选（粒子层扩展覆盖标题栏区域，让粒子在标题栏背后漂浮）：视觉更统一但需改动分层结构、粒子 bounds、鼠标转发与爆发动画坐标系，且按钮区背景动态粒子影响可读性；超出本次诉求，放弃。

## Risks / Trade-offs

- [标题栏悬停按钮圆底协调性] → 悬停圆底为白色 α28，在 BG_TOP 深色底上观感与变更前一致；关闭按钮悬停红不变。
- [回归面] → 改动集中于 TitleBar 一处绘制 + UiConstants 一处常量删除，无结构改动，其他页面零触碰。

## Migration Plan

纯视觉常量与绘制变更，无数据迁移。回滚 = 移除 TitleBar 背景填充、恢复 TITLE_BAR_BG 常量；不影响功能与终端模式。
