## Context

进度条填充值来自 `parsePercent(data.progress)` 的快照值:每次 `refresh()` 快照到达都 `rows.clear()` 后按序重建 `RowData`,再由 `buildRow` 新建 `MosaicBar` 组件——新实例无历史显示状态,填充随快照瞬间跳到目标值。既有 33ms 共享 Timer 仅推进明灭相位 phase(见 gui-effort-progress-bar 设计)。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 填充值以缓动动画平滑追赶目标进度(指数平滑 ease-out,约 0.5s 趋稳),完成时平滑填充至 100%。
- 行数据原地更新、进度条组件实例跨快照复用,动画状态不丢失。

**Non-Goals:**

- 不改马赛克/脉冲/轨道/百分比文本/失败与 0% 行为/行排序/统计小窗等其他已确定实现。
- 百分比文本仍显示快照真实进度,不参与动画。

## Decisions

### D1: 行数据原地更新 + 组件实例复用(ProgressPanel)

`refresh()` 由「clear 后全量重建」改为按源文件路径匹配原地更新:已存在行更新各字段(progress/result/state/size/eta/targetFile),新增行创建,消失行移除;排序与序号重排逻辑不变。`RowData` 增加 `final MosaicBar bar` 字段(创建时以当前进度为目标值);`buildRow` 仅对既有实例重新 setBounds 布局,不再新建组件。`begin()` 仍全量清空重建(新操作、动画状态归零)。

### D2: 缓动追赶动画(MosaicBar)

`MosaicBar` 持两个进度值:`target`(快照目标 0-100)与 `display`(当前显示 0-100,初始同 target)。共享 Timer 每 tick 先推进 phase,再对每行调用 `bar.tick()`:`display += (target - display) * 0.12`(指数平滑,ease-out 先快后慢,约 20 帧/0.66s 至 90%);`|target - display| < 0.5` 时直接归位 target,避免永动。`paintComponent` 的 fillEnd 用 `display` 计算;明灭脉冲与轨道绘制不变。备选:线性插值固定时长 → 放弃,指数平滑实现最简且观感自然;Swing Timer 线程更新字段+repaint 安全(EDT)。

### D3: 行为边界

- `begin()`:新行 target=display=0,无动画起始。完成:`finish()` 成功行 target=100 → 平滑填满;失败行 target=失败时刻进度 → 停留不跳变。0% 行 target=0 → 无填充。等待行 target=0 ✓。
- 百分比文本:仍取 `resolvePercent(data.progress)` 快照值(真实进度),不随动画走——文本真实、条体平滑。
- 快照频率高时:动画持续追赶属预期表现(延迟感即目标)。

## Risks / Trade-offs

- [refresh 原地更新引入行增删匹配回归] → 匹配键为源文件路径(唯一),验收核对:重复操作行不残留、行数=当前文件数、排序不变。
- [动画与快照频率的竞态] → 单 EDT Timer 串行推进,无并发问题;快照间隔远大于 33ms 时动画自然结束在最新目标。
- [失败行目标不变动画不动] → target 与 display 相等时 tick 无效果,失败行稳定停留,与既有要求一致。

## Migration Plan

纯 GUI 变更,无数据迁移;回滚即恢复 refresh 全量重建与直接以快照值绘制,不影响功能与终端模式。
