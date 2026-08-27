## Why

GUI 操作流程存在三处体验问题:提交操作前强制弹出文件清单确认,对已通过表单明确意图的用户是多余打断;勾选删除源文件后的确认弹窗逐项罗列文件,文件多时列表溢出,且系统原生弹窗样式与整体暗紫主题格格不入;操作完成后返回表单页,上一轮的输入缓存(文件、密钥等)残留,既不整洁也带来密钥滞留风险。

## What Changes

- 移除 "Confirm files to process" 文件清单确认弹窗:提交表单后直接进入执行阶段,未勾选删除时全程无弹窗(加密与解密模式均适用)。
- 重做删除源文件确认弹窗:仅提示操作完成后将删除源文件(含文件数量),不逐项列出文件路径;弹窗视觉与整体主题一致(紫色渐变背景、主题风格按钮);语义不变——选择保留文件仅取消删除、操作继续。
- 操作完成展示结果后,用户返回表单页时表单全量重置回初始状态:清空已选文件、密钥、确认密钥、目标目录、删除/仅本机选项与错误提示,加/解密模式回到 Encrypt、算法回到 AES;操作失败返回表单时保留输入以便修正重试。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 移除「操作前文件确认」要求(文件清单确认弹窗删除);新增「删除源文件确认」(不列文件清单的主题化弹窗)与「完成后表单重置」要求。

## Impact

- `gui/interfaces/swing/EncryptDogFrame.java`: 删除 confirmFilesDialog 方法及其调用,confirmDeleteDialog 替换为 ThemedConfirmDialog,backToForm 调用表单重置。
- `gui/interfaces/swing/EncryptFormPanel.java`: 新增 reset() 清空输入缓存并复位模式与算法。
- `gui/interfaces/swing/PasswordToggleField.java`: 新增 clear() 清空密码(兼容明文/掩码态)。
- `gui/interfaces/swing/ThemedConfirmDialog.java`: 新增主题化确认弹窗组件。
- 不涉及 core、终端模式路径与加解密行为变更。
