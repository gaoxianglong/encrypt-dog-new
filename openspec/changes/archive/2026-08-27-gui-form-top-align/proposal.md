## Why

首页的 Encrypt/Decrypt 分段选择器与文件拖拽区当前左边缘与输入字段对齐（x=134），比下方 "Secret key" 等字段标签（x=30）缩进了一截。用户要求两者的左边缘拉到与 "Secret key" 文字相同的位置，视觉上与标签列左对齐。

## What Changes

- Encrypt/Decrypt 模式切换栏左边缘 134 → 30（与 "Secret key" 等字段标签起始位置对齐），宽度 420 → 524，右边缘（554）与输入字段右边缘保持一致。
- 文件拖拽区左边缘 134 → 30，宽度 420 → 524，右边缘与输入字段右边缘保持一致。
- 其余表单控件（选择按钮行、Secret key、确认密钥、Algorithm、Target directory、选项、主按钮、版本号）位置不变；执行页与终端模式不变。

## Capabilities

### New Capabilities

- `swing-gui`: 新增「表单首行与拖拽区左对齐」要求。

### Modified Capabilities

<!-- 无修改能力 -->

## Impact

- `gui/interfaces/swing/EncryptFormPanel.java`: modeToggle 与 dropPanel 的 x/width 调整。
- 不涉及 core、终端模式与其他组件。

## 前置依赖

本变更基于未归档变更 `gui-home-clean-overlay`（表单上移 50px 后 y 坐标）。归档顺序：先归档 `gui-home-clean-overlay`，再归档本变更。
