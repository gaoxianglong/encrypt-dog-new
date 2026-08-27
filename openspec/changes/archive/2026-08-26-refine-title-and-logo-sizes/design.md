# Design: 标题字号与 logo 尺寸协调

## Context

当前尺寸（见 UiConstants 与各使用点）：

- 任务标题 "Encrypt files"：`LOGO_FONT_SIZE`=26pt 加粗，bounds (30,7,400,34)（头部块 y7..57 在容器 y0..64 内居中，见 center-header-title-block）
- 标题栏品牌名称 "EncryptionDog"：`TITLE_BAR_FONT_SIZE`=22pt 加粗
- 标题栏 logo：`LOGO_TITLE_SIZE`=24，BoxLayout(X_AXIS) 垂直居中，标题栏高 46px

动机见 proposal.md - Why。`LOGO_FONT_SIZE` 仅被任务标题使用（头部重构遗留命名）；`LOGO_TITLE_SIZE` 仅被 TitleBar 使用。

## Goals / Non-Goals

**Goals:**

- 任务标题字号降至不高于品牌名称（22pt）
- 标题栏 logo 放大至与品牌文字视觉高度相当（28px）
- 头部块 bounds 与居中几何零变化；下方模块几何零变化

**Non-Goals:**

- 不改副标题、按钮、表单字号
- 不改颜色、文案、图标资源
- 不触碰终端模式与核心逻辑

## Decisions

### D1. 任务标题 26→22（与品牌同号）

- 22pt 与品牌名称同号，任务标题不再比品牌更大，层级自然恢复；bounds (30,7,400,34) 不变，34px 标签容纳 22pt 字形绰绰有余，头部块 50px 居中几何零变化。
- 备选：24（减幅小，"太大"的观感仍在）→ 拒；20（层级更分明但任务标题可能偏小）→ 若用户后续反馈再调。

### D2. 标题栏 logo 24→28

- 46px 标题栏内 28px logo 上下各留 9px，视觉高度与 22pt 品牌文字（字形约 26px）相当；BoxLayout 垂直居中无需改动。
- LogoUtil 已按显示尺寸精确缩放图标（12.9 修复），放大无裁切风险。
- 备选：30（距栏边仅 8px、高出文字视觉高度，略冒进）→ 拒。

### D3. 常量更名 LOGO_FONT_SIZE → TASK_TITLE_FONT_SIZE

- 该常量头部重构后仅服务任务标题，原名（logo 字号）名不副实；更名涉及 UiConstants 定义处与 EncryptFormPanel 一处引用，共 2 行。

## Risks / Trade-offs

- [任务标题与品牌同号，层级靠位置区分] → 品牌位于标题栏且有 logo 组合，权重天然更高，可接受；不满意可后续降至 20。
- [logo 28px 是否过大] → 46px 栏内留 9px 边距，无屏渲染验证视觉高度相当。

## Migration Plan

纯增量，无数据/格式变化；回滚 = revert 本 change 提交。

## Open Questions

（无）
