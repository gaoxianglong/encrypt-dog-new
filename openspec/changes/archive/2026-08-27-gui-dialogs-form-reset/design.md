## Context

当前提交流程(`EncryptDogFrame.handleSubmit`):`prepareOperation` 解析文件 → `confirmFilesDialog` 文件清单确认(JOptionPane)→ 勾选删除时 `confirmDeleteDialog` 列表式二次确认(JOptionPane)→ `startOperation`。两个弹窗均为系统原生 JOptionPane,与暗紫主题不符;`backToForm` 恢复表单面板时不重置任何字段。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 提交无多余打断:未勾选删除时提交直达执行。
- 删除确认主题化(紫色渐变背景、主题按钮)且不列文件清单。
- 完成返回后表单无残留输入;失败路径保留输入供重试。
- 重置后模式与算法一并回到初始状态(Encrypt/AES)。

**Non-Goals:**

- 不改动终端模式与 core。
- 不搭建通用对话框框架,只做本次需要的 `ThemedConfirmDialog`。

## Decisions

### D1: 移除文件清单确认,流程收敛为三步

`handleSubmit` 中删除 `confirmFilesDialog` 调用与方法体,流程变为:解析文件 → (勾选删除? 主题化删除确认) → 执行。`appService.previewTargetFiles` 保留不动(避免改动面扩散,未来预览功能可复用);`JOptionPane` import 随之移除(该文件内两处 JOptionPane 均为本次移除/替换对象)。

### D2: 新建 ThemedConfirmDialog 替代 JOptionPane

新建 `gui/interfaces/swing/ThemedConfirmDialog`:

- 无边框圆角模态 `JDialog`(与主窗口同为 frameless + RoundRectangle 风格);
- 内容面板绘制 `BG_TOP → BG_BOTTOM` 渐变(与 GlassCardPanel 同源配色常量),主文案 `TEXT_PRIMARY`、副文案 `TEXT_SECONDARY`;
- 底部按钮复用 `GradientButton`:主按钮(Confirm delete)ACCENT 渐变,次按钮(Keep files)次要描边样式;
- 相对主窗口居中,关闭按钮/ESC 等价于「保留文件」;
- 返回语义与旧版一致:true=确认删除,false=保留文件。

替代方案:定制 JOptionPane(否决——无法注入渐变背景与自绘按钮,样式受限)。

### D3: 弹窗内容仅提示后果与数量

主文案随模式:"After encryption, N source files will be deleted." / "After decryption, N source files will be deleted."(N = `prepareOperation` 返回的 `files.size()`);副文案沿用旧语义提示:"Choosing Keep files only cancels the deletion. The operation continues."。不再拼接文件路径清单(多文件时溢出问题的根因)。

### D4: 表单重置集中在 EncryptFormPanel.reset()

新增 `reset()`:`fileListModel.clear()`(JList 绑定共享 model,clear 即回空态)、`secretKeyField.clear()`、`confirmKeyField.clear()`、`targetPathField.setText("")`、两个复选框取消勾选、`clearError()`;随后 `modeToggle.setSelectedIndex(0)`(回到 Encrypt,联动确认密钥行显隐、标题文案与布局重排)与 `algorithmCombo.setSelectedItem("AES")`(回到缺省算法,XOR 提示联动隐藏)。**口径已确认**:全量重置回启动初始状态(用户选择)。若当前已是 Encrypt/AES,同值设置不触发事件(SegmentedToggle 有同索引守卫,JComboBox 对同选中项不派发 ItemEvent),幂等安全。

配套:`PasswordToggleField` 新增 `clear()`,同时清空掩码 `passwordField` 与明文 `plainField`,兼容明文显示态(现仅 `toggle()`/`getPassword()`,无清空接口)。

### D5: 调用点与失败路径

`backToForm` 的 fadeCard swapAction 中恢复 formPanel 后调用 `reset()`(与现有 `clearError()` 同处);`operationFailed` 不调用 `reset()`,保留输入供修正重试。

## Risks / Trade-offs

- [删除弹窗样式主观验收] → 全部取自 UiConstants 现有配色常量,验收时截图对照主题一致性。
- [移除文件清单确认后误操作回旋余地变小] → 表单拖拽框已展示所选文件,删除动作仍有二次确认兜底;这是用户明确要求的简化。
- [reset 时拖拽框淡入动画残留行索引] → 空 model 无行可绘,无影响;如后续需要可扩展动画复位。
- [明文态下密码清空不彻底] → `clear()` 同时清两框,由无头验证覆盖两种显示态。

## Migration Plan

纯 GUI 变更,无数据迁移;回滚即恢复 JOptionPane 弹窗与不重置行为,不影响已加密产物与终端模式。
