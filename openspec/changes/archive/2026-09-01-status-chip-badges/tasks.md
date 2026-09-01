# 实现任务：执行页状态列胶囊徽章

## 1. StateChip 自绘组件

- [x] 1.1 在 `ProgressPanel` 内新增 `StateChip extends JComponent`：`paintComponent` 绘制圆角矩形低透明度状态色底色（Waiting=TEXT_SECONDARY@15%、Running=ACCENT_BRIGHT@20%、Finished=SUCCESS_GREEN@20%）+ 1px 同色描边（alpha 40–50%）+ 状态色文字（11pt bold，沿用 `displayState()` 的首字母大写文案），高 22px 在行高内垂直居中、宽=FontMetrics 实测文字宽+2×10 内边距、圆角=高/2。验证：`mvn compile` 通过，三态配色 switch 覆盖 WAITING/RUNNING/FINISHED。
- [x] 1.2 三态静态渲染（视觉验收反馈取消呼吸动画）：`fillAlpha()` 固定返回 Waiting 15%/Running 20%/Finished 20%，不读取相位、无动画。验证：`mvn compile` 通过，无新增 Timer/线程。

## 2. 行构建接线

- [x] 2.1 `RowData` 增加 `chip` 字段（构造时创建，与 `bar` 同模式复用），`buildRow` 中 `chip.setState(data.state)` 后挂载于 State 列（替代原 `stateLabel`），行重建时实例复用不重复创建。验证：编译通过；连续快照刷新后徽章实例引用不变（同 `bar` 行为）。
- [x] 2.2 列宽比调整：`COLUMN_RATIOS` 中 State 75→92、Progress 300→283（`RATIO_TOTAL` 1151 不变）。验证：宽屏 1400 窗口下 State 列约 103px，"Finished" 胶囊含内边距完整显示、无截断，进度条与百分比文本不受影响。

## 3. 视觉与行为验收

- [x] 3.1 启动 GUI（`java -jar dog.jar --gui`）执行一次多文件加密：进入执行页全部行显示灰色 Waiting 胶囊；执行中行显示紫色 Running 胶囊（静态）；完成行变薰衣草白 Finished 胶囊。验证：与 spec 场景「状态徽章渲染」「状态徽章静态渲染」「完成回填」一致。
- [x] 3.2 制造一个失败文件（如目标目录不可写）：失败行状态徽章保持 Finished 薰衣草白，Result 列显示红色错误小图标（悬停展示原因），不出现 FAILED 徽章。验证：与 spec 场景「状态徽章渲染」一致。
- [x] 3.3 返回表单后再次执行新操作：行不残留/不重复，状态徽章从 Waiting 重新开始。验证：与「重复操作行不残留」场景一致。
- [x] 3.4 回归确认：行按状态排序（RUNNING > WAITING > FINISHED）不变；进度条四色脉冲/缓动/百分比、长路径 `...` 截断与悬停、列表紫色滚动条均不受影响。
- [x] 3.5 终端模式回归：不带 `--gui` 执行一次加/解密，终端行为与改动前完全一致（Dashboard 渲染不受影响）。
