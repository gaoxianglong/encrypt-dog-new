# Design: 修复密码框 Tab 焦点被内嵌按钮截获

## Context

（动机见 proposal.md - Why。）

现状：`PasswordToggleField`（`interfaces/swing/PasswordToggleField.java`）是 BorderLayout 的 JPanel，内嵌 `passwordField`（CENTER）+ `toggleButton`（EAST，眼睛切换，`setFocusPainted(false)`）。Swing 默认 `LayoutFocusTraversalPolicy` 将眼睛按钮视为焦点停靠点：Tab 从密码框出发第 1 次落在眼睛按钮上（无焦点环、看似无反应），第 2 次才离开。目标目录框 `targetBox`（`EncryptFormPanel.java:257-270`）内嵌 `browseButton`，同机制。全 GUI 无任何自定义 `FocusTraversalPolicy`（已 grep 确认），算法下拉框是单一组件故 1 次 Tab 正常。`toggle()` 切换后调用 `requestFocusInWindow()` 维持焦点在输入框内，与本变更无冲突。

## Goals / Non-Goals

**Goals:**

- 密码框与目标目录框内的辅助按钮不再截获 Tab，全表单 Tab 一步直达下一个可聚焦控件
- 按钮点击功能（眼睛切换、浏览目录）与视觉不变
- 掩码态与明文态下 Tab 行为一致

**Non-Goals:**

- 不改表单整体焦点顺序（独立控件如选择文件/删除选中/提交按钮仍保留在 Tab 序列中）
- 不引入自定义 `FocusTraversalPolicy` 或键盘助记符体系
- 不改核心加解密与终端模式

## Decisions

### D1: 内嵌辅助按钮 `setFocusable(false)`（方案 A）

- `PasswordToggleField` 构造器内 `toggleButton.setFocusable(false)`；`EncryptFormPanel` 内 `browseButton.setFocusable(false)`。各 1 行。
- **为什么选方案 A**：Swing 中 `setFocusable(false)` 后按钮不再进入焦点循环，但鼠标点击仍正常触发 ActionListener；Tab 从密码框出发时遍历策略直接跳到面板后的下一个可聚焦控件；Tab 进入面板时焦点落在唯一可聚焦子组件（掩码态 `passwordField`、明文态 `plainField`）——三种焦点路径全部一步完成，无新增类、无每实例策略接线。
- **方案 B（否决）**：为面板实现自定义 `FocusTraversalPolicy` 跳过按钮。可达同样效果，但需新增策略类并对每个复合控件实例接线，代码量数倍于方案 A；且保留按钮键盘可达性在本应用中无既有需求（全部按钮均为鼠标驱动），收益为零。
- **方案 C（否决）**：`setFocusTraversalKeysEnabled(false)` 禁用组件自身的 Tab 键，语义错误（会连带破坏该组件继续向后遍历），且对按钮无效。
- **已知取舍**：眼睛/Browse 按钮失去键盘触发路径（可访问性下降），记录于 Risks。

### D2: 修复范围覆盖两类复合控件

眼睛按钮与 Browse 按钮是同一焦点截获机制，且都位于「Tab 从输入框出发」路径上（密码框→下一字段、目标目录框→复选框）。只修眼睛按钮会在目标目录框留下同样的两次 Tab 体验，违背新 spec 需求「输入类复合控件内嵌辅助按钮不截获 Tab」的统一契约。独立按钮（选择文件/目录/删除选中/提交）不属于复合控件内嵌按钮，保留在 Tab 序列中。

### D3: 与 `toggle()` 焦点保持逻辑的关系

`toggle()` 在掩码/明文切换后调用对应输入框的 `requestFocusInWindow()`，与按钮是否可聚焦无关，逻辑不变。明文态下 `plainField` 是唯一可聚焦子组件，Tab 行为与掩码态对称，满足 spec「掩码态与明文态行为一致」场景。

### D4: spec 边界

Tab 一步切换是用户可观察的键盘交互契约（spec 级），以 1 条 ADDED（键盘 Tab 焦点遍历）+ 1 条 MODIFIED（密码可见性切换，补眼睛按钮不截获 Tab 及场景）落入 swing-gui delta；`setFocusable(false)` 是实现细节，不进 spec。

## Risks / Trade-offs

- [眼睛/Browse 按钮失去键盘触发路径] → 本应用为鼠标优先交互，且按钮功能均有替代路径（密码可见性无键盘需求、目标目录可直接键入）；如未来要求键盘可访问，再引入自定义遍历策略或助记符，不影响本次契约
- [FlatLaf 对非聚焦按钮的点击态渲染变化] → `setFocusable` 不影响绘制，`focusPainted` 本就为 false；验收时回归眼睛按钮切换后图标与提示、Browse 按钮 hover/按下视觉
- [遗漏其他复合控件内嵌按钮] → 全 GUI 仅 `PasswordToggleField`（×2 实例）与 `targetBox` 两处文本字段+内嵌按钮复合结构（grep 确认），tasks 中逐实例列明验证项

## Migration Plan

- 纯 GUI 焦点行为调整，无数据迁移；回滚即移除两行 `setFocusable(false)`。
