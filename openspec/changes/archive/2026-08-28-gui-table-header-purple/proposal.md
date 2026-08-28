## Why

执行页表格表头背景现为深紫(`DEEP_ACCENT #3A2A66` 半透明),用户要求改用主色系的紫色,与按钮/进度条/滚动条等强调组件统一。表头文字、圆角与布局不变。

## What Changes

- 执行页表格表头背景 SHALL 改为半透明主色紫(`ACCENT #AF97E5`,透明度保持现有 150),替换深紫 `DEEP_ACCENT`。
- 表头文字颜色/字号/居中对齐、表头圆角与高度均 SHALL 保持不变。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 修改「执行表格视图」要求——表头背景改为主色系半透明紫。

## Impact

- `gui/interfaces/swing/ProgressPanel.java`: `HeaderBand` 绘制色由 `DEEP_ACCENT` 改为 `ACCENT`(α150)。
- 不涉及 core、终端模式与其他组件。
