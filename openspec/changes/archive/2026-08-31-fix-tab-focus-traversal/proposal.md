# Proposal: 修复首页密码框 Tab 焦点被内嵌按钮截获

## Why

首页密码输入框需要按 2 次 Tab 才能切换到下一个文本框，而算法下拉框 1 次即可 —— 密码框是 `PasswordToggleField` 复合控件（JPanel 内嵌 `JPasswordField` + 眼睛按钮），Swing 默认焦点遍历把内嵌眼睛按钮也当作焦点停靠点：第 1 次 Tab 焦点移到眼睛按钮上（按钮 `setFocusPainted(false)` 隐藏了焦点环，看起来「没反应」），第 2 次 Tab 才离开密码框进入下一个控件。目标目录框（`targetPathField` + Browse 按钮）存在同机制隐患。此问题破坏表单的键盘连续填写体验。

## What Changes

- 「Secret key」与「Confirm secret key」密码框内的眼睛切换按钮不再参与 Tab 焦点遍历：Tab 从密码框 1 次直达下一个控件（确认密码框 / 算法下拉框）
- 目标目录框内嵌的 Browse 按钮同样不再参与 Tab 焦点遍历（同机制，保持全表单 Tab 一步切换一致）：Tab 从目标目录框 1 次直达「删除源文件」复选框
- 按钮的鼠标点击功能不受影响（眼睛切换、浏览目录仍可点击）；密码框掩码/明文切换后的焦点保持逻辑（`toggle()` 内 `requestFocusInWindow`）不变
- 无 BREAKING 变更；不触碰核心加解密与终端模式

## Capabilities

### New Capabilities

<!-- 无 -->

### Modified Capabilities

- `swing-gui`: 「密码可见性切换」需求变更 —— 眼睛按钮不进入 Tab 焦点序列；新增「键盘 Tab 焦点遍历」需求 —— 输入类复合控件内嵌的辅助按钮不截获 Tab，Tab SHALL 一步到达下一个可聚焦控件

## Impact

- **代码**：`PasswordToggleField`（`toggleButton.setFocusable(false)`）、`EncryptFormPanel`（`browseButton.setFocusable(false)`）；仅 2 处各 1 行，无其他组件或布局改动
- **验收**：加密/解密模式分别从密码框、算法下拉框、目标目录框按 Tab 逐一验证一步切换；眼睛按钮与 Browse 按钮点击功能回归
- **不涉及**：核心加解密、终端模式、执行表格页、其他 GUI 组件
