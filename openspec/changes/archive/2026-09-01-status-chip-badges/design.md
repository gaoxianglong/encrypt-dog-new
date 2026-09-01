# 设计：执行页状态列胶囊徽章

## Context

执行页 `ProgressPanel` 是纯自绘面板：null 布局 + 自绘组件（`RowStrip` 行条、`HeaderBand` 表头、`MosaicBar` 进度条），行组件按进度快照 `rebuildRows()` 重建，但 `RowData` 对象按源文件路径复用（进度条实例随行保留动画状态）。面板已有一个 33ms 的 `pulseTimer` 推进共享相位 `phase`（每 tick +0.14，约 1.48s 一个完整周期）并只重绘行区 `rowsPanel`。State 列现状为纯文字 `JLabel`（列宽比 75/1151，宽屏下约 84px），三态颜色：WAITING=TEXT_SECONDARY 灰、RUNNING=ACCENT_BRIGHT 紫、FINISHED=TEXT_PRIMARY 白。

动机与范围见 proposal.md；需求见 specs/swing-gui/spec.md。

## Goals / Non-Goals

**Goals:**

- State 列以半透明圆角胶囊徽章呈现三态（主题同源阶梯：灰/紫/薰衣草白），与行条、统计小窗的玻璃质感同源
- 三态徽章静态渲染（视觉验收反馈取消呼吸动画），动画仅保留给进度条
- 徽章宽度在状态列内自适应，不引入水平滚动

**Non-Goals:**

- 不引入 FAILED 红色徽章（用户已确认保持三态，失败语义仍归 Result 列错误图标）
- 不改状态展示文案（仍为 Waiting/Running/Finished，沿用 `displayState()`）
- 不引入真实 `JButton`（无需点击/焦点语义）
- 不触碰统计小窗、进度条、表头等执行页其余部分

## Decisions

### D1：新增自绘 `StateChip extends JComponent`，而非 JButton

徽章与 `RowStrip`/`MosaicBar` 同风格自绘：`paintComponent` 内画圆角矩形填充 + 1px 描边 + 文字。否决 `JButton` + 自定义 UI：引入焦点/点击/回车语义，行单元格需要完全惰性组件；否决纯 `JLabel` + 背景色 hack：不支持低透明度底色与描边分离控制。徽章实例存于 `RowData`（与 `bar` 同模式），`buildRow` 时 `setState(data.state)` 后重新挂载——重建行只改状态不丢动画状态（动画状态本就由共享 `phase` 推导，无实例态）。

### D2：胶囊视觉配方（具体数值，apply 时可微调）

- 尺寸：高 22px（行高 46px 内垂直居中），宽 = `FontMetrics` 实测文字宽 + 2×10 内边距，居中于 State 列；圆角 = 高/2（全胶囊形）
- 文字：11pt bold（当前单元格 12pt plain，胶囊内略收一档更精致），颜色取各状态全饱和度主题色
- 填充底色 alpha：Waiting 15%、Running 20%（呼吸基准）、Finished 20%；1px 描边 alpha 40–50%；文字与描边同色系，保证行条（白 10/255）之上辨识度
- 配色为主题同源阶梯（视觉验收反馈：SUCCESS_GREEN 满饱和绿在紫灰主题中突兀，弃用）：灰（沉睡）→ 亮紫（活跃）→ 薰衣草白（归于平静），绿色语义仅保留给 Result 列对勾图标，状态列与结果列语义分离

| 状态 | 底色 | 描边/文字 |
| --- | --- | --- |
| Waiting | TEXT_SECONDARY @15% | TEXT_SECONDARY |
| Running | ACCENT_BRIGHT @20% | ACCENT_BRIGHT |
| Finished | TEXT_PRIMARY @20% | TEXT_PRIMARY |

### D3：三态静态渲染（视觉验收反馈：取消呼吸动画）

初版 Running 徽章读取 `ProgressPanel.this.phase` 做底色透明度呼吸（12%↔30% 起伏，周期约 1.48s），视觉验收后用户要求取消——徽章改为三态静态：`fillAlpha()` 固定返回 Waiting 15%/Running 20%/Finished 20%，不读相位、无新增绘制负担。`pulseTimer` 仍仅服务于马赛克进度条脉冲。

### D4：列宽比调整 State 75→92，Progress 300→283

宽屏（执行页恒为 1400×880 宽形态）下 State 列约 84px→103px："Finished" 11pt bold 约 53px + 2×10 内边距 ≈ 73px，余量充足。Progress 列 283 仍 ≥ 进度条固定 253px + 偏移 + 百分比文本所需 ~311px/1.119≈278px，够用；列宽比总和 1151 不变，其余列零改动。

### D5：状态色表通过 switch 内联于 StateChip

三态 ×（底色 alpha/描边/文字色）共 9 个常量，量级不足以抽出独立枚举或配置表；`StateChip` 内私有静态方法按 `state` 字符串返回配色三元组。未来若加第四态（如 FAILED），此处单点扩展。

## Risks / Trade-offs

- [徽章 + 马赛克脉冲同帧重绘增加绘制量] → 每行仅多 1 次 `fillRoundRect` + 1 次 `drawRoundRect` + 1 次文字绘制，相对既有马赛克方格绘制可忽略；行数上限由滚动列表控制在可视 ~10 行。
- [11pt bold 文字宽超出状态列（极端缩放）] → 宽屏形态下列宽比固定，实测余量 >25px；如遇极小宽度兜底：内边距收缩至 6px 而非溢出。
- [徽章静态与进度条动画并存] → 状态徽章不参与动画，动态反馈完全由进度条承担，页面动效焦点单一不打架，属设计意图。
- [RowStrip 白 10/255 底上 15% alpha 灰徽章对比度偏低] → 描边 40–50% alpha 兜底轮廓，文字全饱和度；apply 后视觉验收微调 alpha 即可（仅改常量）。

## Migration Plan

纯视觉渲染变更，无数据模型、持久化或跨版本兼容问题；回滚即还原 `ProgressPanel` 与列宽比常量。
