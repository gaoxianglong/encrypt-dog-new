## Why

主题化删除确认弹窗上线后,渐变背景与主题按钮已生效,但弹窗四角仍残留系统默认的灰色直角底色,与圆角玻璃质感、整体暗紫主题不一致。

## What Changes

- 修正 `ThemedConfirmDialog` 的四角渲染:窗口自身背景置为全透明,内容面板与根面板 `setOpaque(false)`,保留圆角裁剪 `setShape`,使弹窗四角呈现透明圆角,不再出现灰色直角。
- 渐变背景与描边 SHALL 与弹窗圆角一致,不超出圆角范围。
- 仅影响删除源文件确认弹窗;主窗口(EncryptDogFrame)与其他界面不变。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 新增「主题化弹窗圆角渲染」要求——主题化确认弹窗四角为透明圆角,不出现系统默认灰色直角底色。

## Impact

- `gui/interfaces/swing/ThemedConfirmDialog.java`: 窗口背景透明、内容/根面板非不透明,保留 setShape 圆角裁剪。
- 不涉及 core、终端模式与其他界面组件。
