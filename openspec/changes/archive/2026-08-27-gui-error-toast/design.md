## Context

`EncryptFormPanel` 现有错误提示为常驻 `errorLabel`(红色 ERROR_RED,位于主按钮上方,坐标由 `layoutRows` 管理,解密模式随主按钮上移 46px)。`showError`/`clearError` 仅 setText,无消退机制;`operationFailed` 回到表单时调用 `showError` 并触发卡片抖动。用户确认采用气泡 Toast 方案(见 proposal.md「Why」)。

## Goals / Non-Goals

**Goals:**

- 校验失败与操作失败均以主题渐变气泡提示,淡入 → 保持约 3s → 淡出自动消失,不常驻。
- 气泡随模式布局联动;再次失败重新弹出并重新计时。
- 移除红色常驻 `errorLabel`。

**Non-Goals:**

- 不改动校验规则与错误文案内容。
- 不触碰结果页(ResultPanel)的错误展示与终端模式。

## Decisions

### D1: 新建 ToastBubble 组件(单计时器三阶段动画)

新建 `gui/interfaces/swing/ToastBubble`:

- 视觉:ACCENT→ACCENT_BRIGHT 渐变圆角气泡 + 白色文字(BUTTON_TEXT,契合用户"白色更好"的倾向且与主题一致),底部绘制小三角箭头指向主按钮;气泡圆角与弹窗/卡片圆角风格一致。
- 动画:单个 `javax.swing.Timer`(FRAME_DELAY_MS 16ms,与 fadeCard/shakeCard 同模式)按 `System.currentTimeMillis` 计算阶段:淡入 300ms(alpha 0→1)→ 保持至 3300ms → 淡出 800ms(alpha→0)→ `setVisible(false)` 并停表。整体可见时长约 4s。
- 重复 `show(message)`:更新文案、重置起始时间、`setVisible(true)` 并重启计时(含展示期间再次调用)。
- 气泡宽度按文案 FontMetrics 自适应,设最大宽度上限,超出换行至最多两行;水平居中对齐主按钮中心。

替代方案:行内换色+淡出(否决——用户已选定气泡形态);JToolTip/PopupFactory(否决——不可控绘制、难以主题化)。

### D2: 气泡挂载于 EncryptFormPanel,展示时按主按钮坐标定位

气泡作为面板子组件(null 布局)。气泡宽高由文案决定、仅在 `show` 时确定,因此定位放在 `showError` 中按当前 `submitButton.getY()` 计算(水平居中、位于按钮上方间距 8px),两种模式的联动由主按钮坐标天然保证;不登记到 `layoutRows`(尺寸未知时登记只能取到过期值)。

### D3: showError/clearError 改为驱动气泡,移除 errorLabel

- `showError(message)` → `toastBubble.show(message)`;`clearError()` → `toastBubble.hide()`。
- 删除 `errorLabel` 字段、构造样式与 `layoutRows` 中的坐标登记,改为登记气泡 bounds。
- `reset()` 已调用 `clearError()` → 气泡隐藏,无需额外处理;`operationFailed` 仍走 `showError` + 抖动,气泡与抖动并存。

## Risks / Trade-offs

- [气泡短暂遮挡按钮上方区域] → 气泡位于按钮上方、不覆盖按钮本身,宽度受限,不影响点击;用户已接受该形态。
- [长文案溢出] → 自适应宽度+最大宽度上限,超长换行最多两行;极端超长文案截断由上限兜底。
- [计时器与 EDT] → 与现有动画同为 Swing Timer,全在 EDT,无线程问题;`show` 重复调用幂等(重置起点)。
- [无头验证的局限] → 无头断言覆盖气泡显示状态与解密模式坐标;淡入淡出节奏由 GUI 实测确认。

## Migration Plan

纯 GUI 变更,无数据迁移;回滚即恢复常驻红字提示,不影响功能行为与终端模式。
