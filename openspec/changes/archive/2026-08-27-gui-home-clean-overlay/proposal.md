## Why

首页（表单页）蒙层目前带主副标题块，且蒙层自带的顶部高光描边带（60px 圆角描边）底边形成一条横向分割线，把蒙层分成上下两部分。用户要求首页改造：只保留一个干干净净的蒙层——取消主副标题、取消分割线。

## What Changes

- 取消蒙层上的任务主标题（"Encrypt files"/"Decrypt files"）与副标题（"Protect…"/"Restore…"）。
- 取消蒙层顶部高光描边带（GlassCardPanel 的 topHighlight），首页蒙层与执行页一样为干净统一的半透明面，无横向分割线；topHighlight 开关随之删除（两个页面都不再需要）。
- 表单内容整体上移约 50px 填补标题块空出的空间：Encrypt/Decrypt 模式切换成为蒙层首行，下方各模块（文件拖放区、Secret key、Algorithm、Target directory、选项、主按钮）随之上移，卡片顶部不出现空洞；解密模式的确认密钥行隐藏与整体上移动逻辑不变。
- 卡片右下角版本号保持原位不动。
- 执行页布局、终端模式均不变。

## Capabilities

### New Capabilities

- `swing-gui`: 新增「标题栏 logo 与品牌名称协调」要求（承接原「字号层级」中保留的条款）。

### Modified Capabilities

- `swing-gui`: 修改「信息层级」要求——主内容区为干净统一蒙层，不再展示主副标题，首行即模式切换；修改「模式切换布局联动」要求——标题块取消后内容整体上移至卡片顶部；移除「头部排版」与「字号层级」要求（主副标题已取消）。

## Impact

- `gui/interfaces/swing/EncryptFormPanel.java`: 移除主副标题标签与常量，表单内容整体上移 50px。
- `gui/interfaces/swing/GlassCardPanel.java`: 移除顶部高光带开关，常画干净半透明面。
- `gui/interfaces/swing/EncryptDogFrame.java`: 移除 `setTopHighlight` 两处调用。
- 不涉及 core、终端模式。

## 假设

- 「只保留一个干干净净的蒙层」指蒙层表面干净统一（无高光带/分割线），表单字段与按钮等表单内容保留；版本号不在「主副标题」之列，保留于卡片右下角。
