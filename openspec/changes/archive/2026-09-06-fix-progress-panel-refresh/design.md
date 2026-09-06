## Context

执行页（`ProgressPanel`）每秒收到一次进度快照（core 的 `ViewSchedule` → `GuiDashboardView.draw()` → EDT 上的 `refresh()`），`refresh()` 末尾调用 `rebuildRows()`：对行容器 `rowsPanel` 执行 `removeAll()` → `setBounds()` → `setPreferredSize()` → 重加全部行 → `revalidate()`。同时 `refresh()` 按状态重排序（RUNNING > WAITING > FINISHED），正在加密的文件永远排在列表第一行。

已用最小 Swing 复现实验（JDK 21）定位到两个缺陷的机制：

1. `removeAll()` 之后只要再执行 `setBounds`/`setPreferredSize` + `revalidate()`，`JViewport` 的 viewPosition 被重置为 (0,0)，而滚动条 BoundedRangeModel 的值不变——视口显示列表顶部（即正在加密的文件）、滚动条却仍指向原位置，两者脱钩。仅 `removeAll()` + 重加 + `revalidate()`（不动尺寸）时位置保留。
2. 处理期间 `Elapsed` 显示 GUI 时钟 `(now - beginMs)/1000`（`beginMs` 在 `begin()` 记录，位于 worker 线程启动之前）；完成时 `finish()` 改用 core 的 `result.getTimeConsuming()`，其起点在 `EncryptProxy.invoke()` 内——中间隔着 worker 启动、`buildOperationVOList` 与 `new EncryptProxy()` 构造函数里的 Reflections 类路径扫描（每次操作都执行，约数秒）。两把时钟起点相差约 3 秒，完成瞬间数值倒退。

`timeConsuming` 的 GUI 消费者仅 `ProgressPanel.finish()` 一处；终端 Dashboard（`ViewRenderUtil`）与 core 内部（`FinishedListener`）不受影响，本次不触碰。托盘通知不使用该字段。

## Goals / Non-Goals

**Goals:**

- 进度刷新重建列表时视口位置与滚动条瞄定位置保持不变，用户不被拉回列表顶部。
- Elapsed 统计小窗在处理期间与完成态使用同一计时起点（GUI 时钟），数值单调不减。

**Non-Goals:**

- 不改 core 计时（终端 Dashboard 的耗时语义与显示保持不变）。
- 不做行组件原地更新重构（每帧全量重建保留，属性能优化范畴，另行评估）。
- 不改行按状态排序的既有语义（像素锚定下，行内容随排序自然变动仍属预期）。

## Decisions

### 决策 1：保存/恢复视口位置（方案 A）

`rebuildRows()` 在 `removeAll()` 前保存 `scrollPane.getViewport().getViewPosition()`，在 `revalidate()` 之后调用 `setViewPosition(saved)` 恢复。实验证实：重建 + `revalidate()` 后恢复位置稳定生效（可见区域仍为列表底部），且 `setViewPosition` 会触发 stateChanged 使滚动条模型与视口重新同步，消除脱钩。

- 关键顺序：恢复必须在 `revalidate()` 之后——重建期间的校验会把 viewPosition 重置，提前恢复会被覆盖。
- 替代方案 1：尺寸未变时跳过 `setBounds`/`setPreferredSize`。实验证实仅 `removeAll()` + 重加不重置位置，但它对行数/面板宽度变化等路径无保护，且不修复已存在的脱钩；作为硬化手段与方案 A 叠加（尺寸未变时跳过冗余调用，降低每帧成本），不作为主修复。
- 替代方案 2：行组件原地更新（不 `removeAll`，仅增删变化行、更新既有行内容），滚动位置天然不丢，但改动面大、与"复用进度条/徽章实例"的既有设计耦合，列入 Non-Goals 留待后续性能优化时评估。

保存/恢复 SHALL 仅在 `refresh()`/`finish()` 路径生效；`begin()` 进入新操作时 SHALL 在重建后将视口显式重置到顶部（`setViewPosition(0,0)`）。原因：`progressPanel`/`scrollPane`/`rowsPanel` 实例跨操作复用，面板从卡片摘除（`backToForm` 的 `glassCard.removeAll()`）不会清零 JViewport 的 viewPosition，无条件恢复会把上一轮操作的滚动位置带进新列表。实现上给 `rebuildRows` 增加恢复开关（如 `rebuildRows(boolean restorePosition)`），`begin()` 传 false 并显式置顶。

### 决策 2：Elapsed 统一用 GUI 时钟

`finish()` 不再用 `result.getTimeConsuming()` 覆盖显示，改为 `statElapsed.setValue(Utils.currentTimeFormat(elapsedSeconds()))`——与 `refresh()` 完全同一口径（`beginMs` 起点一致）。完成瞬间数值为"从提交操作到完成"的完整耗时，恒不小于完成前最后一次刷新显示值。

- 替代方案 1：把 core 的 `begin` 提前到 `facade.execute()` 入口。会改变终端 Dashboard 展示的耗时语义（`timeConsuming` 同时供终端使用），波及 core 行为，风险与收益不成比例。
- 替代方案 2：取 GUI/core 两值较大者。掩盖问题而非消除双时钟，且实践中 GUI 值恒大于 core 值，退化为 GUI 时钟，不选。
- `OperationResultDTO.timeConsuming` 字段保留不动（core 照常计算、DTO 照常填充），仅 GUI 执行页不再消费它。若未来托盘通知等新消费方需要该字段，需另行评估口径。

## Risks / Trade-offs

- [恢复时机写错（恢复早于 revalidate）→ 位置仍被重置] → 在 `rebuildRows()` 内以"保存 → 重建 → revalidate → 恢复"固定顺序实现，并用大量文件场景回归验证。
- [行数变化（文件快照增删行）导致列表高度变化 → 恢复的位置超出新范围] → `setViewPosition` 自带范围钳制，恢复到新范围的最近位置，可接受。
- [像素锚定下，行内容仍会随每秒重排序在锚定位置下换文件（如新完成的行落到底部）] → 既有排序语义保留，符合预期；用户可自由滚动，不被强行拉回顶部即为本变更目标。如需"跟踪指定行"体验，另立变更。
- [完成瞬间与最后一次刷新的显示可能有 1 秒内差异（EDT 延迟、秒级整数截断）] → 单调不减不受影响（GUI 时钟终点晚于最后一次刷新），可接受。

## Migration Plan

无需迁移。GUI 单点行为修正，`pom.xml` 无依赖变化，构建产物结构不变。回滚方式为 revert 本次提交。
