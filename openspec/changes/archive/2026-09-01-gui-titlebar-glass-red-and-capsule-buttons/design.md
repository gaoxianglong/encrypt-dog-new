# 设计：标题栏玻璃红悬停与表单分层胶囊按钮

## Context

标题栏 `TitleBar.WindowButton` 悬停时自绘圆形底色：关闭按钮用实底红 `TITLE_BAR_CLOSE_HOVER` #E05555（全窗唯一高饱和红块），最小化按钮用白 28/255 半透明圆。表单小按钮经 `EncryptFormPanel.createSmallButton()` 统一创建为幽灵样式（透明底、ACCENT_BRIGHT 文字、无描边、无手型光标），共 4 个调用点：+ Select files、+ Select directory、Remove selected、Browse…。主按钮 `GradientButton` 为紫色渐变实底 + 白字 bold 14 + 弧 22 + 悬停提亮 0.18 + 加载旋转弧。小按钮当前无悬停/禁用态处理。

动机与范围见 proposal.md；需求见 specs/swing-gui/spec.md。

## Goals / Non-Goals

**Goals:**

- 关闭按钮悬停改为与主题玻璃质感一致的半透明玻璃红，保留警示语义
- 表单按钮建立三段层级：渐变主按钮 > 实底胶囊（添加类）> 幽灵（辅助类）
- 胶囊按钮具备完整状态机：常态/悬停/禁用

**Non-Goals:**

- 不改 Execute 主按钮（渐变、字号、加载态均不动）
- 不改最小化按钮悬停（白 28/255 玻璃圆不变）
- 不给幽灵按钮（Remove/Browse）增加悬停态（保持现状，若后续需要另开变更）
- 不触碰执行页/标题栏其它元素

## Decisions

### D1：关闭按钮玻璃红 = #E05555 @ alpha 45/255

`UiConstants.TITLE_BAR_CLOSE_HOVER` 改为 `new Color(0xE0, 0x55, 0x55, 45)`，色相不动（满足「语义色不变」约束），仅从实底变玻璃。否决的替代：白圆统一（丢失危险暗示，关窗点错成本高）；莓果红换色相（改动更大且引入新色）；红字无底（悬停反馈弱化）。alpha 45 为初值，视觉验收偏淡时调至 60——单常量可调。

### D2：新增 `CapsuleButton extends JButton`，不复用 GradientButton

`GradientButton` 耦合了渐变画笔与 loading 旋转弧（表单提交专属语义），复用会带脏。新建 ~70 行自绘类，绘制骨架照抄 GradientButton：`fillRoundRect` 扁平主色 + 悬停 `lighten(0.18)` + 禁用 `BUTTON_DISABLED`，差异点：扁平 ACCENT 单色（非渐变，亮度低于 Execute 渐变尾端 ACCENT_BRIGHT，层级靠亮度区分）、弧=高/2 全胶囊（26px → 13）、字号 SMALL_FONT_SIZE 12、手型光标。

### D3：工厂拆分与调用点归属

`createSmallButton(text)` 保持幽灵实现不动；新增 `createCapsuleButton(text)` 返回 CapsuleButton。调用点：`+ Select files`、`+ Select directory` → 胶囊；`Remove selected`、`Browse…` → 幽灵。归类依据：动词性质——"+" 开头为添加类主路径操作，Remove/Browse 为辅助/低频操作；Remove 保留幽灵也避免"删除"被紫色实底弱化警示性。

### D4：禁用态防御性实现

当前代码无任何 `setEnabled(false)` 作用于小按钮（已 grep 确认），但 CapsuleButton 仍实现禁用置灰（BUTTON_DISABLED 填充），成本两行，未来加禁用逻辑时行为正确。

## Risks / Trade-offs

- [扁平 ACCENT 胶囊与 Execute 渐变主按钮的亮度层级] → ACCENT 比渐变尾端 ACCENT_BRIGHT 深一档，视觉验收确认主次分明；若仍抢戏可降为 ACCENT 加深或减小胶囊高度。
- [玻璃红 45/255 在暗色标题栏上辨识度] → 保留红⾊色相 + 悬停字形变白双重信号；验收偏淡调 60。
- [幽灵按钮无悬停反馈与胶囊按钮并存的不一致感] → 层级意图（辅助类低调）即为设计目标；若验收觉得别扭，后续变更给幽灵加"悬停文字提亮"即可。
- [CapsuleButton 与 GradientButton 代码重复（圆角/提亮/禁用）] → 两处各 ~20 行绘制逻辑，规模不值得抽象基类；保持平行实现。

## Migration Plan

纯视觉渲染变更，无数据/持久化/兼容性问题；回滚即还原 TitleBar 常量、EncryptFormPanel 调用点并删除 CapsuleButton。
