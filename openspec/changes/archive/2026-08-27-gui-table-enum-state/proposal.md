## Why

执行表格的 State 列当前显示 GUI 自推导的小写状态（waiting/running/done），与核心枚举 `EncryptStateEnum` 定义好的状态（WAITING/RUNNING/FINISHED）不一致。用户要求 State 列显示枚举常量中定义好的状态。

## What Changes

- State 列取值改为核心枚举 `EncryptStateEnum` 定义的状态：WAITING（初始过程）、RUNNING（执行中）、FINISHED（已完成）。
- ACL 在进度快照组装时把 `DashboardViewState.state` 的枚举状态值写入快照，GUI 直接展示；快照缺失状态值时回退到现有推导逻辑，保持兼容。
- 操作完成路径（成功或失败）的行状态为 FINISHED，成功/失败仍由 Result 列区分。
- 行排序语义不变：RUNNING > WAITING > FINISHED；状态配色语义不变。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 修改「执行表格视图」要求——State 列状态取值改为核心执行状态枚举定义的状态。

## Impact

- `gui/application/dto/OperationProgressDTO.java`: `FileProgress` 增加 `state` 字段（枚举状态值字符串）。
- `gui/infrastructure/acl/EncryptCoreFacade.java`: 快照组装时拷贝 `vs.getState().getState()`。
- `gui/interfaces/swing/ProgressPanel.java`: 状态常量改为 WAITING/RUNNING/FINISHED，`resolveStatus` 优先取快照状态。
- 不涉及 core、终端模式。

## 前置依赖

本变更基于未归档变更 `gui-execution-overlay-layout`、`gui-execution-overlay-move-up`、`gui-execution-overlay-nudge-down`。归档顺序：三者依次归档后再归档本变更。
