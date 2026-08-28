## Context

执行表格视图全部集中在 `gui/interfaces/swing/ProgressPanel.java` 自绘实现:9 列列宽由 `COLUMN_RATIOS`(总和 `RATIO_TOTAL=1066`)按面板可用宽度等比缩放(`columnLayout`);进度列当前仅放 `GradientBar`(140 宽比),无百分比文本;Source/Target 列用宽度驱动的 `ellipsis`(保留前缀、尾部加 `…`)截断;State 列直接展示核心枚举透传值 `WAITING/RUNNING/FINISHED`,排序优先级(`statePriority`)与配色均按原始值判断。终端 Dashboard 的路径截断语义为 `Utils.lengthShear(path, 25)`(保留末 25 字符、前缀 `...`,见 `ViewRenderUtil.java:134/242`)。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 进度列加宽并增加百分比文本(未开始显示 `-`),Source File / Target File 列宽收敛让出空间,其余列基本不动。
- Source/Target 路径截断与终端语义一致:末 25 字符 + 前缀 `...`,tooltip 仍展示完整路径。
- State 展示形态首字母大写其余小写(Waiting/Running/Finished),内部值、排序、配色判断不变。

**Non-Goals:**

- 不改核心枚举与终端模式任何代码(纯展示层变更)。
- 不改表格行排序规则、失败行进度行为、结果图标、滚动条、统计小窗、蒙层布局等其他已确定实现。
- 不改变截断字符数语义(固定 25,与终端一致,不做宽度自适应)。

## Decisions

### D1: 进度列加宽并内嵌百分比文本(ProgressPanel)

列宽比由 `{36, 230, 85, 85, 75, 140, 85, 230, 100}` 调整为 `{36, 195, 80, 80, 75, 215, 80, 195, 110}`(总和仍 1066):进度列 140→215,Source/Target 各 230→195,Before/After/ETA 85→80,Result 100→110(图标列略宽)。其余列宽基本不变,表头与行自动按新比例重排。

`buildRow` 中进度单元格改为"条 + 文本"组合:百分比 `JLabel`(12F、`TEXT_SECONDARY`、左对齐)固定占宽 50px、垂直居中、紧贴条右端(间隙 4px);`GradientBar` 位于列左内边距后,宽度 = 列宽 − 左右内边距(16) − 50 − 间隙(4)。文本取值直接用 `data.progress`(核心透传的 `45.6%` 形态),占位行为 `-` 原样展示,不额外解析。

备选:右对齐 48px 文本框 → 首次验收发现视觉间距随文本长度变化(`-` 时最远),用户要求贴近进度条;改为左对齐紧贴条右端后间距恒定为 4px。

备选:把百分比画进 `GradientBar` 自绘内部 → 放弃,文本位置随条宽变化不稳,独立 `JLabel` 布局直观且复用现有 `cellLabel` 字体规范。

### D2: 路径截断切换为终端语义(ProgressPanel)

`buildRow` 中 Source/Target 文本由 `ellipsis(data.source, layout[3], font)` 改为宽度自适应的尾段截断 `shearToFit(text, width, font)`:超出可用宽度(列宽 − 12)时从文本前端逐字丢弃,前缀 `...`,末尾字符完整显示——与终端 Dashboard 的 `Utils.lengthShear` 同语义(保留末段、前缀 `...`),但保留字符数随列宽自适应,保证末尾不被裁切;tooltip(`setToolTipText`)不变,仍展示完整路径。原宽度驱动的 `ellipsis` 方法被 `shearToFit` 替代删除。

备选:固定 `Utils.lengthShear(path, 25)` → 首次验收发现 195px 列宽下 `...`+25 字符超出单元格、末尾被裁切,不符合"后面完整显示"的验收要求;改为宽度自适应后末尾始终完整,且与终端按列宽截断的行为一致。

### D3: 状态标题化展示(ProgressPanel)

新增静态方法 `displayState(String state)`:空值原样返回;否则首字母大写、其余小写(`substring(0,1).toUpperCase(Locale.ROOT) + substring(1).toLowerCase(Locale.ROOT)`)。仅在 `buildRow` 构造状态单元格时调用 `displayState(data.state)`,`RowData.state` 仍存原始值——`statePriority` 排序、`STATUS_RUNNING/STATUS_WAITING` 配色判断全部按原始值进行,零行为变化。核心枚举、DTO 透传链路不动。

备选:在 `resolveStatus`/DTO 转换层直接改写状态值 → 放弃,会破坏排序与配色比较,且污染与核心一致的数据语义;展示层映射是唯一安全位置。

## Risks / Trade-offs

- [极窄窗口下前段丢弃过多] → 截断长度随列宽自适应,末尾始终完整显示;窗口极窄时保留字符数变少、前缀 `...` 占比增大,以验收实测为准。
- [48px 百分比文本占位对极窄窗口的挤压] → 列宽按比例缩放,窗口极窄时进度条变短但不换行不遮挡;百分比固定 48px 保证完整显示。
- [改动范围回归] → 仅 D1/D2/D3 三处,均在 ProgressPanel 内,其他页面与 core 零触碰。

## Migration Plan

纯 GUI 展示变更,无数据迁移、无持久化格式变化;回滚即恢复原 `COLUMN_RATIOS`、原 `ellipsis` 调用与原状态文本,不影响功能行为与终端模式。
