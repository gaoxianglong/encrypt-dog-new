## Context

核心已有执行状态枚举 `EncryptStateEnum`（WAITING 初始过程 / RUNNING 执行中 / FINISHED 已完成），核心进度视图 `DashboardViewState.state` 持有该枚举。当前 GUI 未消费该字段：`EncryptCoreFacade` 组装快照时不拷贝 state，`ProgressPanel` 自推导小写状态（waiting/running/done）。用户要求 State 列显示枚举中定义好的状态。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- State 列显示核心枚举定义的状态：WAITING / RUNNING / FINISHED。
- 状态经 ACL 从核心透传到 GUI，应用层不依赖 core 类型。

**Non-Goals:**

- 不改枚举本身与核心状态流转。
- 不改行排序优先级语义与状态配色语义。

## Decisions

### D1: DTO 用 String 携带状态值（与 result/errorMsg 同模式）

`OperationProgressDTO.FileProgress` 增加 `state` 字段（字符串），值为 `EncryptStateEnum.getState()`（"WAITING"/"RUNNING"/"FINISHED"）。应用层不引入 core 类型，保持「GUI 应用层不依赖 core」的既有边界。

### D2: 枚举→字符串换算在 ACL 边界完成

`EncryptCoreFacade`（基础设施层，唯一依赖 core 类型的层）在快照组装循环中 `fp.setState(vs.getState().getState())`。完成路径不拷贝状态：`ProgressPanel.finish()` 直接把行状态置为 FINISHED（成功与失败同属已完成，由 Result 列区分成败）。

### D3: ProgressPanel 状态常量与推导逻辑对齐枚举

- 常量改为 `STATUS_WAITING="WAITING"`、`STATUS_RUNNING="RUNNING"`、`STATUS_DONE="FINISHED"`。
- `resolveStatus(fp)` 优先取 `fp.getState()`；为空时回退现有推导（SUCCESS/FAILED→FINISHED、进度空→WAITING、其余→RUNNING），兼容任何未带状态的快照来源。
- `statePriority`、状态配色仅匹配新常量值，排序/配色语义不变。

## Risks / Trade-offs

- [快照来源未透传状态] → resolveStatus 保留回退推导,行为不劣化。
- [大写状态文本列宽] → State 列文字变长(如 FINISHED),列宽按比例布局,数值居中,验收确认无截断。

## Migration Plan

纯 GUI 数据透传与展示变更,无数据迁移;回滚即恢复自推导逻辑。
