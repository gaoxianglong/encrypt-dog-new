## Context

`ToastBubble` 气泡形态(实心渐变→毛玻璃模糊)两轮验收均不通过。用户最终确认:错误提示改为**内联展示**——显示在出错字段内部,主题色,约 2 秒逐步淡出。既有的 `GuiAnchor` 错误锚点映射保留,仅替换提示载体与定位方式。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 提示以主题色内联文字出现在出错字段内部,约 2 秒逐步淡出,不常驻。
- 锚点语义与模式联动不变;重复失败重新计时。
- 删除气泡组件与背景模糊采样机制。

**Non-Goals:**

- 不改动校验规则与错误文案。
- 不触碰结果页与终端模式。

## Decisions

### D1: 新建 FieldHint 内联提示组件(替代 ToastBubble)

- 视觉:亮薰衣草紫文字(`0xC0B8FF`,比 ACCENT_BRIGHT 更浅、不显深)+ 半透明深色背衬(`0x2B1B5C` α170 圆角),保证与字段既有内容重叠时清晰可读;不使用红色。
- 动画:单 Swing Timer,展示 1.2s(alpha 1)→ 淡出 0.8s(alpha→0)后 `setVisible(false)`,总时长约 2s;重复 `showHint` 重置计时(与既有动画同模式)。
- 绘制:自绘背衬圆角矩形与文字,整体走同一 AlphaComposite 淡出,不调用 `super.paintComponent`(避免 JLabel 自绘文字不受 alpha 控制)。

### D2: 按锚点定位到字段内部

`EncryptFormPanel.showError(message, anchor)`:

- 尺寸:按文案 FontMetrics 自适应,高度固定 26(不超过字段高),宽度上限按锚点预留——密钥/确认密钥右侧预留 52px(眼睛按钮),算法预留 24px(下拉箭头),其余预留 12px。
- 定位:密钥/确认密钥/算法 → 字段内部左侧 6px、垂直居中;文件 → 拖拽区顶部条带居中(y+6,避开空态图片与文案所在的垂直中部);系统/执行失败 → 主按钮上方居中(按 `submitButton.getY()` 实时取值,随模式联动)。
- **z 序**:构造中 `add(fieldHint)` 后显式 `setComponentZOrder(fieldHint, 0)` 置顶。实测踩坑:该面板中提示曾渲染在拖拽面板下层,空态图标/文案盖住提示;置顶后经像素级验证确认提示为最上层。同时 `FieldHint` 继承 `JComponent` 而非 `JLabel`,避免 BasicLabelUI 重复绘制文字。
- 锚点组件坐标随加/解密模式联动,展示时实时取值即可,无需登记 layoutRows。

### D3: 移除气泡与模糊采样

删除 `ToastBubble.java`(含 JLayeredPane 粒子采样、盒式模糊、箭头绘制);`EncryptFormPanel` 字段由 `toastBubble` 替换为 `fieldHint`;`clearError` → `fieldHint.dismiss()`,`reset()` 联动不变。

## Risks / Trade-offs

- [提示与字段既有内容重叠] → 半透明深色背衬 + 主题紫文字保证短暂重叠期间可读;约 2 秒自动消失,影响可控。
- [长文案在窄字段内截断] → 宽度按锚点上限裁剪;文案本身保持简短,极端超长仅显示截断部分(可接受)。
- [淡出节奏验收] → 1.2s+0.8s 常量集中,验收不符一处调整。

## Migration Plan

纯 GUI 变更,无数据迁移;回滚即恢复气泡提示,不影响功能行为与终端模式。
