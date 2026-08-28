## Context

执行表格的百分比文本(`ProgressPanel.buildRow` 中紧贴进度条的 `JLabel`)直接展示 `data.progress`(核心 `DashboardViewState.progress` 透传,缺省 `-`)。核心仅在 `ProgressEvent` 到达时更新进度(`ProgressListener`),文件在产生任何进度之前失败(如魔数校验失败)时进度保持 `-`,GUI 百分比显示 `-`,与等待行无法区分。已确认的既有要求「失败行进度条停留在失败时刻进度」仅约束进度条填充,不涉及百分比文本。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 失败且进度为 `-` 的行百分比文本显示 `0%`;进度条填充行为不变(`-` 与 `0%` 解析结果均为 0)。
- 失败时已有中途进度的行仍显示失败时刻进度;等待行仍显示 `-`。

**Non-Goals:**

- 不改核心数据链路、`DashboardViewState`、进度事件与终端 Dashboard(终端模式行为不变)。
- 不改进度条填充、状态映射、排序与配色等其他已确定实现。

## Decisions

### D1: 展示层百分比归一化(ProgressPanel)

新增静态方法 `resolvePercent(String progress)`:进度为 `-`/空白时返回 `"0%"`,其余原样返回。该规则同时覆盖等待行与失败前未产生进度的行——即"所有行以 0% 起始展示";产生过进度的行(含中途失败的行)原样展示失败时刻/实时进度,与既有「失败行进度条停留在失败时刻进度」要求一致(用户确认)。仅在 `buildRow` 构造百分比 `JLabel` 时把 `data.progress` 替换为 `resolvePercent(data.progress)`;`GradientBar` 仍用 `parsePercent(data.progress)`(`-`→0,与显示一致,无需改动)。`refresh` 与 `finish` 两条数据路径都经 `buildRow` 渲染,单点归一化即可覆盖执行中与完成回填两种时机;`RowData` 数据不变。

备选:核心侧将缺省进度置为 `0%` → 放弃,会改变终端 Dashboard 显示,违反「终端模式行为不变」要求,且 GUI 之外改动面大;展示层归一化最小且语义完整。

## Risks / Trade-offs

- [归一化规则与「失败时刻进度」要求的边界] → 仅 `-`/空白归一化为 `0%`,有中途进度的失败行不受影响(用户确认:中途失败停留原进度),两条要求无冲突;验收时以一次"产生进度前即失败"(如非 dog 文件解密)、一次"中途失败"(如错误密钥解密)与等待行初始态实测确认。
- [改动范围回归] → 仅 D1 一处,ProgressPanel 内单点,其他页面与 core 零触碰。

## Migration Plan

纯 GUI 展示变更,无数据迁移、无持久化格式变化;回滚即恢复百分比标签直接展示 `data.progress`,不影响功能行为与终端模式。
