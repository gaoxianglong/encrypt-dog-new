## Why

错误提示经历气泡形态(实心渐变、毛玻璃)两轮迭代后仍不理想。经用户最终确认:放弃气泡/弹窗形态,改为**内联提示**——错误信息直接显示在出错字段内部,主题色渲染,约 2 秒逐步淡出消失,简洁且不遮挡布局。

## What Changes

- 新增 `FieldHint` 内联提示组件:主题紫(ACCENT_BRIGHT)文字 + 半透明深色背衬(与字段内容重叠时可读),展示约 1.2 秒后逐步淡出,总时长约 2 秒;重复提示重新计时。
- 提示定位到出错字段内部:密钥/确认密钥错误显示在对应输入框内(右侧留出眼睛按钮),算法错误显示在算法下拉框内,源文件错误显示在文件拖拽区内居中;无字段错误(系统不支持、执行失败)以同款内联提示显示在主按钮上方。
- 删除 `ToastBubble` 气泡组件及其背景模糊采样机制。
- 淡入淡出节奏约 2 秒、随加/解密模式布局联动;终端模式行为不变,不涉及 core。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 新增「错误提示内联展示」要求——错误提示显示在出错字段内部,主题色,约 2 秒逐步淡出,不使用气泡/弹窗;保留错误→字段锚点语义。

## Impact

- `gui/interfaces/swing/FieldHint.java`: 新增内联提示组件(主题色、背衬、淡出计时)。
- `gui/interfaces/swing/EncryptFormPanel.java`: 以 `FieldHint` 替换 `ToastBubble`,按锚点定位到字段内部。
- `gui/interfaces/swing/ToastBubble.java`: 删除。
- 锚点枚举(`GuiAnchor`)与异常携带逻辑不变;不涉及 core、终端模式。
