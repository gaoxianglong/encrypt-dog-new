## Why

GUI 表单在切换到 Decrypt 模式时存在两处体验缺陷：任务标题与副标题仍保持加密文案（"Encrypt files / Protect your files with local encryption"），与当前模式不符；确认密钥行隐藏后，Secret key 与 Algorithm 之间留下约一行高度的空洞，破坏表单视觉节奏。本次变更修正这两处，使解密模式的表单信息与布局均与模式一致。

## What Changes

- 任务标题与副标题随 Encrypt/Decrypt 模式联动：加密模式为 "Encrypt files" / "Protect your files with local encryption"，解密模式为 "Decrypt files" / "Restore your files with local decryption"，标题块位置不变。
- 解密模式下确认密钥行（标签 + 输入框）隐藏后，其下方全部模块（Algorithm、Target directory、两个选项复选框、错误提示、主操作按钮）整体上移一个行高，消除空洞；加密模式布局与现状完全一致。
- 卡片右下角版本号保持原位不动（两种模式均固定于卡片右下角）。
- 终端模式行为不变，不涉及 core 代码。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 修改「信息层级」要求——任务标题与副标题随模式切换联动；修改「头部排版」要求——模式切换时其下方模块随确认密钥行显隐联动重排（解密模式上移一个行高）。

## Impact

- `gui/interfaces/swing/EncryptFormPanel.java`：标题/副标题由构造函数局部变量提升为字段，`switchMode` 统一驱动文案与布局；新增解密模式下的模块坐标重排逻辑。
- `gui/interfaces/swing/constant/UiConstants.java`（如需要）：提取确认密钥行行高常量，供重排偏移量复用。
- 不涉及 core、终端模式路径与任何加解密行为变更。
