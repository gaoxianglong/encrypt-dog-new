# Design: 拖拽框增高与解密模式隐藏仅本机选项

## Context

（动机见 proposal.md - Why。）

现状：表单页为固定坐标布局（null layout），卡片 580×600 位于 760×760 方形窗口内；拖拽框 `dropPanel.setBounds(LABEL_X, 54, FIELD_X + FIELD_WIDTH - LABEL_X, 112)`（`EncryptFormPanel.java:192`），其下全部控件 y 坐标硬编码。模式切换机制已存在：`switchMode(isEncrypt)` 控制确认密钥行显隐，`layoutRows(isEncrypt)` 以 `offset = isEncrypt ? 0 : ROW_PITCH` 统一上移下方模块。`-o` 复选框 `onlyLocalCheckBox`（`EncryptFormPanel.java:154`）当前两种模式都展示。核心解密路径已确认完全不读取 `isOnlyLocal`（已归档的 secret-key-storage 变更验证），解密展示该选项无意义。

## Goals / Non-Goals

**Goals:**

- 拖拽框可视高度提高 1/3（112 → 150），文件列表视野明显改善
- 解密模式表单不展示「仅本机」选项，布局无空洞，加密模式布局与视觉不变
- 窗口形态体系（方形/宽屏切换、圆角、居中、拖拽 resize 记忆）行为不变

**Non-Goals:**

- 不改核心加解密与终端模式
- 不重构表单布局为动态 layout（保持 null layout + 常量坐标惯例）
- 不改执行表格页与宽屏形态的定位常量（自动适应，无需动）

## Decisions

### D1: 拖拽框 +38px，窗口与卡片同步增高 38px（方案 A）

- `WINDOW_HEIGHT` 760 → 798、`CARD_HEIGHT` 600 → 638，`dropPanel` 高度 112 → 150，其下全部 y 坐标 +38。
- **为什么整体增高而不是压缩间隙**：38px 无处消化 —— 版本号若随表单下移将到 y=616（+14=630），超出卡片 600；行距已紧凑（密钥与确认密钥仅 8px 间隙），压缩会破坏视觉。
- **为什么 CARD_Y 不用改**：`CARD_Y = (CONTENT_HEIGHT - CARD_HEIGHT) / 2 - TITLE_BAR_HEIGHT / 2`，两端同步 +38 后 `(752-638)/2-23 = 34` 与现状 `(714-600)/2-23 = 34` 恰好相同，卡片垂直居中不变，`EncryptDogFrame` 零改动。
- **宽屏形态**：卡片在宽屏下铺满内容区（`w - WIDE_CARD_X*2` × `contentH - WIDE_CARD_Y - WIDE_CARD_BOTTOM_GAP`），窗口高度常量变化自动传导，无需改动。
- **拖拽框自绘**：`DropFilePanel.paintComponent` 全部使用 `getWidth()/getHeight()`，150 高自动适配，无需改绘制代码。

### D2: 表单 y 坐标整体平移 +38

以 `dropPanel` 为界：`modeToggle`（y=14）与 `dropPanel`（y=54）不动，`dropPanel` 以下全部 +38：

| 控件 | 现 y | 新 y |
|---|---|---|
| 按钮行（选择文件/目录/删除选中） | 170 | 208 |
| secretKeyField | 202 | 240 |
| confirmKeyLabel/Field | 256/248 | 294/286 |
| algorithmLabel/Combo/xorWarning | 302/294 | 340/332 |
| targetDirLabel/targetBox | 348/340 | 386/378 |
| deleteCheckBox | 386 | 424 |
| onlyLocalCheckBox | 416 | 454 |
| submitButton | 472 | 510 |
| versionLabel | 578 | 616 |

校验：submit 底部 554 ≤ 616 版本号顶，版本号底部 630 ≤ 卡片 638 ✓。

### D3: 解密模式隐藏仅本机选项，offset 双层

- `switchMode`：`onlyLocalCheckBox.setVisible(isEncrypt)`（与确认密钥行同一处）。
- `layoutRows`：offset 由单一值拆为两层 —— 解密时「删除源文件」上移 1 个行距（`deleteOffset = isEncrypt ? 0 : ROW_PITCH`），主按钮上移 2 个行距（`submitOffset = isEncrypt ? 0 : 2 * ROW_PITCH`）；onlyLocal 行在解密时不可见，其 setBounds 照常调用（坐标为 454 - deleteOffset 即可，避免隐藏组件残留旧坐标）。
- **为什么 delete 与 submit 上移不同行距**：delete 位于 onlyLocal 之上，仅受确认密钥行隐藏影响（1 行距）；submit 受两行隐藏影响（2 行距）。

### D4: 状态与数据语义

- 勾选状态**跨模式保留**：用户在加密模式勾选 -o 后切到解密再切回，勾选仍在（表单记忆，符合用户预期）。
- `buildForm` 解密时 `form.setOnlyLocal(false)`：核心解密路径本就忽略该标志，写 false 保持 DTO 语义干净。
- `--gui -o` 解密 prefill：不特殊处理（核心忽略，无副作用），若表单处于解密模式该选项本就隐藏。

### D5: spec 边界

拖拽框高度为纯视觉常量调整，不构成行为契约，不进入 spec；「解密模式隐藏仅本机选项」是参数收集与布局联动的可观察行为变化，以 MODIFIED 两条需求落入 swing-gui delta。

## Risks / Trade-offs

- [窗口 798 高在小分辨率屏幕（有效高 <800）可能接近/超出屏幕] → 现有窗口已支持拖拽 resize 且有尺寸记忆；本次仅 +38，风险低；验收时在 13 寸屏观察。
- [解密 offset 双层实现遗漏] → 对照 `layoutRows` 逐控件核对上移量；`fieldHint`（错误提示）定位依赖锚点字段坐标，随字段平移自动跟随，需回归解密模式错误提示位置。
- [versionLabel 贴边风险] → 616+14=630 < 638，余 8px，可接受。

## Migration Plan

- 纯 GUI 常量与坐标调整，无数据迁移。
- 回滚：恢复两个常量与 `EncryptFormPanel` 坐标即可，无持久化影响。
