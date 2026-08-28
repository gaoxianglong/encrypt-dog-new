## 1. 进度列加宽与百分比文本

- [x] 1.1 将 `ProgressPanel.COLUMN_RATIOS` 调整为 `{36, 195, 80, 80, 75, 215, 80, 195, 110}`(总和 1066 不变),验证表头与行按新比例重排、进度列明显加宽且 Source/Target 列收敛
- [x] 1.2 `buildRow` 进度单元格增加左对齐百分比 `JLabel`(12F、`TEXT_SECONDARY`、占宽 50px、紧贴条右端间隙 4px),`GradientBar` 宽度压缩为列宽 − 左右内边距 16 − 50 − 间隙 4,验证执行时进度条右侧显示百分比文本(如 45.6%)、占位行显示 `-`

## 2. 路径终端风格截断

- [x] 2.1 Source/Target 单元格文本由 `ellipsis(...)` 改为宽度自适应的尾段截断 `shearToFit(...)`(超出列宽时前缀 `...`、末尾完整显示),并删除被替代的私有 `ellipsis` 方法,验证长路径末尾完整显示且前缀 `...`、未超列宽路径原样展示
- [x] 2.2 验证悬停 Source/Target 单元格 tooltip 仍展示完整路径(截断不影响 `setToolTipText`)

## 3. 状态标题化展示

- [x] 3.1 新增静态 `displayState(String)`(首字母大写、其余小写),仅在 `buildRow` 状态单元格文本处调用,验证 WAITING/RUNNING/FINISHED 展示为 Waiting/Running/Finished
- [x] 3.2 验证映射后行为不变:行排序仍按原始值 RUNNING > WAITING > FINISHED、状态配色(进行中强调紫/等待次要色)判断仍按原始值

## 4. 验收

- [x] 4.1 `mvn -q compile` 编译通过;GUI 实测一次加密与一次解密,核对:进度条右侧百分比、长路径前缀 `...` 且末尾完整显示、状态 Waiting/Running/Finished、tooltip 完整路径,且统计小窗/结果图标/滚动条/蒙层布局无回归(对照 spec 中「进度条与百分比」「长路径终端风格截断」「表格实时刷新」场景)
