# swing-gui Specification (delta)

## ADDED Requirements

### Requirement: 键盘 Tab 焦点遍历

输入类复合控件（由文本字段与内嵌辅助按钮组成的控件，如密码框、目标目录框）SHALL 保证按下 Tab 时焦点一步直达下一个可聚焦控件：内嵌辅助按钮 SHALL NOT 截获 Tab 焦点，其功能仅通过鼠标触发；表单内独立控件（如算法下拉框）的 Tab 行为 SHALL 维持一步切换，两种模式下行为一致。

#### Scenario: 目标目录框 Tab 一步切换

- **WHEN** 焦点位于「Target directory」输入框内且用户按下 Tab
- **THEN** 焦点直接移动到下一个可聚焦控件（「删除源文件」复选框），不经过内嵌的 Browse 按钮

#### Scenario: 算法下拉框 Tab 一步切换

- **WHEN** 焦点位于算法下拉框且用户按下 Tab
- **THEN** 焦点直接移动到目标目录输入框

## MODIFIED Requirements

### Requirement: 密码可见性切换

密码输入框 SHALL 内嵌可点击的眼睛图标，支持掩码与明文显示互相切换；切换不得丢失已输入内容。眼睛按钮 SHALL NOT 参与键盘 Tab 焦点遍历（不截获从密码框出发的 Tab，仅通过鼠标触发）。

#### Scenario: 切换明文与掩码

- **WHEN** 用户在密码框输入内容后点击眼睛图标
- **THEN** 密码以明文显示；再次点击恢复掩码显示，已输入内容保持不变

#### Scenario: 提交语义不变

- **WHEN** 密码处于明文显示状态时提交表单
- **THEN** 系统仍以掩码语义处理密钥（不落盘、不回显明文到界面以外）

#### Scenario: 密码框 Tab 一步切换

- **WHEN** 焦点位于「Secret key」密码框内且用户按下 Tab
- **THEN** 焦点直接移动到下一个可聚焦控件（「Confirm secret key」密码框），不经过眼睛按钮；确认密码框内按 Tab 同理直达算法下拉框；掩码态与明文态行为一致
