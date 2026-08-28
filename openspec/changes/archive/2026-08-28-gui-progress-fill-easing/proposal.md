## Why

执行页进度条填充随快照刷新瞬间跳到目标值,视觉生硬。用户希望填充具有延迟且自然的加载效果:以缓动动画平滑追赶目标进度,而非一下跳到某个点。其余效果(马赛克/脉冲/轨道/百分比/失败与0%行为)一律不动。

## What Changes

- 进度条填充 SHALL 以缓动动画追赶目标进度(指数平滑 ease-out、先快后慢、约0.5秒趋稳),不再瞬间跳变;完成时 SHALL 平滑填充至 100%;失败行填充 SHALL 停留在失败时刻进度;未产生进度时 SHALL 保持无填充。
- 刷新机制 SHALL 改为按源文件原地更新行数据并复用行内进度条组件实例(当前每快照重建组件、无法保留动画状态)。
- 百分比文本 SHALL 仍显示快照真实进度(不动画);轨道/马赛克方格/明灭脉冲/行排序/统计小窗等一律不动。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 修改「执行表格视图」要求——进度条填充增加缓动加载动效,新增「进度条缓动加载」场景。

## Impact

- `gui/interfaces/swing/ProgressPanel.java`: `refresh` 原地更新行数据;`RowData` 持有 `MosaicBar` 实例;共享 Timer 每 tick 推进各行动画;`MosaicBar` 持目标/显示双进度值。
- 不涉及 core、终端模式与其他组件。

**归档顺序依赖**:本变更 delta 基于 `gui-effort-progress-bar` 的 spec 变更(马赛克描述与脉冲场景),归档时 SHALL 先归档 `gui-effort-progress-bar` 再归档本变更。
