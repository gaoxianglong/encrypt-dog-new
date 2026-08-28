## Why

执行页表格存在三处显示细节不符合用户预期:进度列只有进度条、看不到百分比数字;Source File / Target File 路径截断与终端效果不一致(终端保留末 25 字符并以前缀 `...` 表示,GUI 目前保留前缀并在尾部加省略号);State 列直接展示核心枚举的全大写文本(WAITING/RUNNING/FINISHED),观感生硬。用户要求这三处对齐终端显示效果、提升可读性。

## What Changes

- 进度列:进度条后方增加百分比文本(未开始显示 `-`);进度条加长(进度列宽比增大),相应收敛其他列宽度,以 Source File 与 Target File 两列让出空间为主。
- Source File / Target File 路径截断改为终端风格:超出列宽时丢弃前段、以前缀 `...` 表示、末尾字符完整显示(保留末段 + 前缀 `...`,与终端 Dashboard 语义一致);悬停 tooltip 仍展示完整路径。
- State 列做纯展示映射:首字母大写、其余小写(WAITING→Waiting、RUNNING→Running、FINISHED→Finished);内部状态值、排序优先级与配色判断逻辑保持不变。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 修改「执行表格视图」要求——进度列展示百分比且进度条加长、文件路径按终端风格截断、状态文本标题化展示。

## Impact

- `gui/interfaces/swing/ProgressPanel.java`: 调整 `COLUMN_RATIOS` 列宽比;`GradientBar` 所在进度单元格增加紧贴条右端的百分比文本并压缩条宽;`ellipsis` 替换为宽度自适应的尾段截断 `shearToFit`(保留末段 + 前缀 `...`,与终端 `Utils.lengthShear` 同语义);新增状态展示映射(首字母大写其余小写)。
- 复用 `utils/Utils.lengthShear`,core 与终端模式零改动(ProgressPanel 已依赖 `Utils.currentTimeFormat`)。
- 不涉及表单页、确认弹窗、标题栏及其他组件。
