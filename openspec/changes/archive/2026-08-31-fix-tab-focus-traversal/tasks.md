# Tasks: 修复密码框 Tab 焦点被内嵌按钮截获

## 1. 内嵌辅助按钮退出 Tab 焦点序列

- [x] 1.1 `PasswordToggleField` 构造器内 `toggleButton.setFocusable(false)`（方案 A，见 design D1）；验证：离屏验证程序确认眼睛按钮退出焦点序列、doClick 仍可切换掩码/明文、已输入内容保留、tooltip 随状态翻转
- [x] 1.2 `EncryptFormPanel` 构造器内 `browseButton.setFocusable(false)`；验证：探针确认 focusable=false 且点击监听器仍在（点击→弹窗路径未改动，弹窗视觉检查并入 3.1）

## 2. Tab 遍历验收（对应 spec 场景）

- [x] 2.1 加密模式：焦点在「Secret key」密码框按 Tab 一次直达「Confirm secret key」密码框，再 Tab 一次直达算法下拉框，再 Tab 一次直达目标目录输入框，再 Tab 一次直达「删除源文件」复选框；验证：离屏验证程序以真实 JRootPane 遍历策略计算序列，全程每步一次、无焦点停在眼睛/Browse 按钮上（ALL PASS）
- [x] 2.2 明文态：眼睛切换为明文显示后，从明文框按 Tab 一次直达下一个可聚焦控件（掩码态同）；验证：离屏验证程序确认两态序列一致（ALL PASS）
- [x] 2.3 解密模式：焦点在「Secret key」密码框按 Tab 一次直达算法下拉框；验证：离屏验证程序确认解密布局（无确认密钥行）下同样成立（ALL PASS）
- [x] 2.4 独立控件 Tab 序列未被误伤：选择文件/目录/删除选中按钮、提交按钮仍可依次被 Tab 到达；模式切换栏为鼠标驱动组件（JPanel 无聚焦 peer，JDK21 遍历策略不纳入 Tab 序列），变更前后均不在序列；验证：离屏验证程序完整走查表单序列（ALL PASS）

## 3. 回归

- [x] 3.1 点击与视觉回归：眼睛按钮图标与提示随状态切换、Browse 按钮 hover/按下视觉正常、密码框提交语义不变（GUI 执行一次加密冒烟）；验证：用户已在 GUI 手动确认，与变更前行为一致
- [x] 3.2 终端模式冒烟：CLI 执行一次加密与一次解密确认行为不受影响（本变更仅动 GUI 焦点设置）；验证：expect 驱动真实 CLI 交互完成加密→解密回环，解密产物与原文 diff 一致
