# 实现任务：标题栏玻璃红悬停与表单分层胶囊按钮

## 1. 关闭按钮玻璃红

- [x] 1.1 `UiConstants.TITLE_BAR_CLOSE_HOVER` 改为 `new Color(0xE0, 0x55, 0x55, 45)`（同色相 alpha 45/255，注释说明"玻璃红、保留警示语义"）。验证：`mvn compile` 通过；TitleBar 悬停绘制处无需改动（引常量）。
- [x] 1.2 启动 GUI 悬停关闭按钮：红色圆底呈半透明玻璃质感（透出标题栏背景）、字形变白，与最小化按钮白色半透明圆同语法；移开后恢复灰字无底色。验证：与 spec 场景「关闭按钮玻璃红悬停」一致。

## 2. CapsuleButton 组件

- [x] 2.1 新增 `gui/interfaces/swing/CapsuleButton.java`：扁平 ACCENT #AF97E5 实底 `fillRoundRect`（弧=高/2）+ 白色文字（SMALL_FONT_SIZE 12）+ 悬停 lighten(0.18) + 禁用 BUTTON_DISABLED + 手型光标，绘制骨架参照 GradientButton（无渐变/无旋转弧）。验证：`mvn compile` 通过。

## 3. 表单按钮分层接线

- [x] 3.1 `EncryptFormPanel` 新增 `createCapsuleButton(text)` 工厂；`+ Select files`、`+ Select directory` 改用胶囊按钮，`Remove selected`、`Browse…` 保持 `createSmallButton` 幽灵样式。验证：`mvn compile` 通过，四个按钮尺寸（130×26）与位置不变。

## 4. 视觉与回归验收

- [x] 4.1 启动 GUI 观察表单页：两个添加类按钮为紫色实底胶囊（白字、全圆角），Remove/Browse 仍为紫色文字幽灵样式，Execute 渐变主按钮层级突出（主 > 添加 > 辅助）。验证：与 spec 场景「主题强调组件着色」一致。
- [x] 4.2 悬停添加类胶囊：底色提亮；点击功能不变（打开文件/目录选择器）。验证：选择器正常弹出。
- [x] 4.3 回归确认：表单布局（按钮位置、字段对齐）、拖拽区、模式切换（Encrypt/Decrypt）不受影响；执行页与状态胶囊徽章不受影响。
- [x] 4.4 终端模式回归：不带 `--gui` 执行一次加/解密，终端行为与改动前完全一致。
