# 标题栏玻璃红悬停与表单分层胶囊按钮

## Why

两处视觉瑕疵：窗口右上角关闭按钮悬停时的**实底红色圆形**（#E05555）是整窗唯一的高饱和红块，与紫色玻璃主题不搭；表单页 "+ Select files" 等小按钮为透明底幽灵样式，缺乏按钮质感与主次层级。探索定案：关闭悬停改玻璃红（保留警示语义）、表单小按钮改分层胶囊（添加类实底、辅助类保持幽灵）。

## What Changes

- 关闭按钮悬停色：`TITLE_BAR_CLOSE_HOVER` 由实底红 `#E05555` 改为**同色相 alpha 45/255 半透明玻璃红**（色相不变，符合「语义色不变」约束；最小化按钮不动）。验收若 45 偏淡可微调至 60。
- 新增自绘 `CapsuleButton`（swing 包）：扁平主色 ACCENT #AF97E5 实底 + 白色文字 + 全圆角（弧=高/2，26px 高 → 弧 13）+ 悬停提亮 0.18 + 禁用置灰 BUTTON_DISABLED + 手型光标。
- 表单按钮分层：
  - `+ Select files`、`+ Select directory`（添加类）→ `CapsuleButton` 实底紫胶囊白字
  - `Remove selected`、`Browse…`（辅助类）→ 保持幽灵样式（ACCENT_BRIGHT 文字）不变
- 层级关系：Execute 主按钮（渐变 + 14 bold）> 添加类（扁平实底 12）> 辅助类（幽灵 12）。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

- `swing-gui`: 「主题主色系」需求扩展：主题强调组件清单加入表单添加类胶囊按钮（主色实底、白字）；窗口关闭按钮悬停 SHALL 以半透明玻璃红圆底呈现（保留警示语义、与玻璃质感一致）；场景「主题强调组件着色」同步扩展。

## Impact

- 受影响代码：`TitleBar.java`（悬停色常量使用处）、`UiConstants.java`（TITLE_BAR_CLOSE_HOVER alpha）、新增 `gui/interfaces/swing/CapsuleButton.java`、`EncryptFormPanel.java`（`createSmallButton` 拆出胶囊变体，4 个调用点按类归属）。
- 无 API 变更、无终端模式变更、无依赖变更。
