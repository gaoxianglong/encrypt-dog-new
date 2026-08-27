## 1. 移除文件清单确认弹窗

- [x] 1.1 在 `EncryptDogFrame.handleSubmit` 删除 `confirmFilesDialog` 调用,并删除 `confirmFilesDialog` 方法与 JOptionPane 相关 import;验证 `mvn -q compile` 编译通过,未勾选删除时提交后无弹窗直接进入进度页

## 2. 主题化删除确认弹窗

- [x] 2.1 新建 `ThemedConfirmDialog`:无边框圆角模态 JDialog、BG_TOP→BG_BOTTOM 渐变背景、主/副文案、GradientButton 主题按钮(Confirm delete 主按钮 / Keep files 次要样式)、相对主窗口居中、ESC/关闭等价保留文件;验证编译通过且弹窗视觉与主题一致(截图对照)
- [x] 2.2 将 `confirmDeleteDialog` 替换为 ThemedConfirmDialog:文案随模式提示 "After encryption/decryption, N source files will be deleted",不列文件路径;验证勾选删除提交后弹窗无文件清单
- [x] 2.3 语义回归:Confirm delete 执行操作并删除源文件;Keep files 仅取消删除、操作继续;验证两种选择各走通一次完整操作

## 3. 完成后表单重置

- [x] 3.1 `PasswordToggleField` 新增 `clear()`,同时清空掩码与明文两框;验证无头断言明文态与掩码态下 `clear()` 后 `getPassword()` 为空
- [x] 3.2 `EncryptFormPanel` 新增 `reset()`:清空文件列表/密钥/确认密钥/目标目录/两个选项/错误提示,模式回到 Encrypt、算法回到 AES;验证无头断言 reset 后各输入为空且 modeToggle 回到 Encrypt、algorithmCombo 回到 AES
- [x] 3.3 `backToForm` 恢复表单后调用 `reset()`,`operationFailed` 不调用;验证完成一次操作后返回表单为空态,失败返回输入保留
- [x] 3.4 终端模式回归:不带 `--gui` 执行一次加密与一次解密;验证终端交互与变更前一致
