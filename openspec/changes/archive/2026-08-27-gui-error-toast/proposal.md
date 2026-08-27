## Why

表单校验失败或操作失败时,主按钮上方的红字错误提示常驻页面不消退,既打断视觉又显得突兀;红色(ERROR_RED)与整体暗紫主题调性不符。经用户确认,采用气泡 Toast 方案:主题渐变气泡自动展示、自动消退。

## What Changes

- 新增 `ToastBubble` 组件:主题渐变圆角气泡(ACCENT 渐变 + 白色文字),底部带指向主按钮的小箭头,淡入 0.3s → 保持约 3s → 淡出 0.8s 后自动消失,不常驻页面。
- 表单错误提示(校验失败与操作失败返回表单)改由气泡展示:再次失败时重新弹出并重新计时;操作失败的气泡与卡片抖动动画并存。
- 移除常驻红色 `errorLabel` 及其相关坐标与样式逻辑。
- 气泡位置随加/解密模式布局联动(解密模式与主按钮同步上移一个行距)。
- 终端模式行为不变,不涉及 core。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 新增「错误提示气泡自动消退」要求——主题渐变气泡在主按钮上方展示错误提示,约3秒后淡出自动消失,再次失败重新弹出,并随模式布局联动。

## Impact

- `gui/interfaces/swing/ToastBubble.java`: 新增主题气泡组件(渐变背景、箭头、淡入淡出计时)。
- `gui/interfaces/swing/EncryptFormPanel.java`: `showError`/`clearError` 改为驱动气泡;移除 `errorLabel` 字段、构造样式与其在 `layoutRows` 中的坐标登记,改为登记气泡位置。
- 不涉及 core、终端模式路径与加解密行为变更。
