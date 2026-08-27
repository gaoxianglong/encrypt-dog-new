# Design: GUI Scrollbar Theming and Animation Polish

## Context

GUI 使用 FlatLaf 3.7.1 暗色主题，滚动条为默认灰白派生色；`DropFilePanel` 的三层动画常量当前为：`PULSE_PERIOD_MS=800`、脉冲颜色 ACCENT↔TEXT_SECONDARY、`RIPPLE_DURATION_MS=350`、涟漪 alpha=70 带宽 80px、`FADE_STEP_MS=40` 步进 0.25。实测反馈：滚动条不协调、动画偏淡偏快。

## Goals / Non-Goals

**Goals:**

- 滚动条（全局）以 ACCENT 紫渲染，与主题一致
- 拖拽动画更持久、更明显（脉冲更亮更慢、涟漪更久更强、行淡入节奏更清晰）

**Non-Goals:**

- 不改动画的行为语义（触发时机、复位逻辑不变）
- 不改滚动条结构（宽度、圆角、AS_NEEDED 策略不变）

## Decisions

### D1. 全局滚动条紫色（FlatLaf UIManager）

- 在 `EncryptDogGui.launch` 的 `FlatDarkLaf.setup()` 之后设置 UIManager 滚动条配色：滑块（thumb）使用 `UiConstants.ACCENT`（#6C63FF），hover/按下态用 `ACCENT_BRIGHT` 提亮，track 保持透明暗色。
- FlatLaf 的 ScrollBar 颜色由 UI defaults 派生，具体键名（如 `ScrollBar.thumbColor` 系列或需经 `FlatLaf` client property）以实现时对 FlatLaf 3.7 的 `FlatScrollBarUI` defaults 实测为准；若标准键不生效，回退方案为自定义 `ScrollBarUI` 子类覆写 thumb 绘制（仅在必要时）。
- 全局生效：文件列表滚动条、算法下拉弹窗滚动条（若出现）及未来所有滚动组件统一紫色——记录假设：用户原话"下拉框滚动条"，实际所见为文件列表滚动条，全局统一是超集且主题一致性更好。

### D2. 拖拽动画参数强化（仅常量/颜色区间调整，触发与复位逻辑不动）

| 动画 | 现值 | 新值 | 效果 |
|---|---|---|---|
| 悬停呼吸脉冲周期 | 800ms | 1200ms | 呼吸更舒缓持久 |
| 脉冲边框颜色区间 | ACCENT ↔ TEXT_SECONDARY(#9B9BC7) | ACCENT ↔ ACCENT_BRIGHT(#8E7BFF) | 整体亮度抬升，不再落入暗灰 |
| 放下涟漪时长 | 350ms | 600ms | 扫过更从容 |
| 涟漪透明度 | 70 | 110 | 更明显 |
| 涟漪带宽 | 80px | 120px | 扫过面积更大 |
| 行淡入步进 | 40ms/步 ×4 | 80ms/步 ×5（步进 0.2） | 每行约 400ms，依次淡入可感知 |

### D3. 文件列表选中态淡紫背景

- `DropFilePanel` 的 JList 设置 `selectionBackground` 为 ACCENT 半透明淡紫（rgba(108,99,255,~80)，在深色底上自然变淡）、`selectionForeground` 为 `TEXT_PRIMARY`。
- `FadeCellRenderer` 需显式绘制选中背景：JList 非 opaque 时 DefaultListCellRenderer 不绘制背景——选中态 `setBackground(淡紫)+setOpaque(true)`，未选中 `setOpaque(false)`；选中行不参与淡入 alpha（既有逻辑保留）。
- 色值决策：淡紫 = ACCENT 降低不透明度（深底上变淡），而非提亮色相——与满饱和 ACCENT 的滚动条/分段选择器/主按钮形成层级区分，避免同色混淆（实测反馈：默认选中色与主题不搭）。

### D4. 窗口底部版权信息

- `EncryptDogFrame` 分层面板中在粒子层之上添加版权 JLabel：bounds（0, 730, 760, 20）、CENTER 对齐、`TEXT_SECONDARY` + SMALL_FONT；位于卡片下方（680..760 底部空白带），粒子动画在其后方继续运行；JLabel 默认不拦截鼠标。
- 文案 "Copyright (c) 2021-2031 gaoxianglong"（与终端 picocli footer 的 Copyright(c) 2021 - 2031 形式一致；如需仅显示 "gaoxianglong" 或更换年份可调）。

## Risks / Trade-offs

- [全局滚动条配色影响所有滚动组件] → 仅视觉变化，主题一致性是本 change 目标
- [FlatLaf 标准键不生效] → D1 回退方案（自定义 ScrollBarUI），工作量可控
- [动画变慢影响高频拖拽体验] → 拖入悬停脉冲为持续动画不影响功能；涟漪为一次性 600ms 不阻塞后续 drop；行淡入与交互并行（选中/移除不受影响）

## Migration Plan

纯增量，无数据/格式变化；回滚 = revert 本 change 提交。

## Open Questions

（无）


