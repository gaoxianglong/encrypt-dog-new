## Why

执行页布局在近期多轮反馈中被改乱:统计小窗行(Files/Success/Failed/Elapsed/Operation)被误删,用户要求恢复此前已确定的形态;同时用户明确要求取消执行页内窗的透明(毛玻璃)效果。本次变更严格限定为:恢复统计小窗 + 取消内窗透明打底,其余部分保持已确定状态、不再改动。

## What Changes

- 恢复五个统计小窗横向等宽铺满(Operation、Files、Success、Failed、Elapsed,高 54、文字居中),随执行进展实时更新;操作类型小窗 SHALL 在进入执行页时立即明确展示当前动作类型(加密为 Encrypt、解密为 Decrypt)。
- 执行页取消内窗透明效果:玻璃卡片不再渲染半透明打底/描边/高光/阴影,表格与统计小窗直接呈现于粒子背景之上;表单页毛玻璃效果保持不变。
- 窗口顶部标题栏(品牌文字、logo、窗口按钮)所有页面常显;home 返回图标位于正式标题栏下方的内容区左上角;内窗无标题层;算法信息与失败错误以内窗顶部小字展示。
- 表格(9 列/表头/进度条/结果图标/行排序/滚动)、失败行进度不强制 100%、完成停留与返回重置均保持已确定状态,不再改动。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 修改「执行表格视图」要求——统计小窗恢复、内窗取消透明打底;修改「执行完成停留」要求——完成呈现引用统计小窗。

## Impact

- `gui/interfaces/swing/ProgressPanel.java`: 恢复五个 StatCard 统计小窗(字段/取值/布局),移除纯文字统计行,状态文字回到顶部。
- `gui/interfaces/swing/GlassCardPanel.java`: 新增毛玻璃开关,执行页关闭渲染(不绘制填充/描边/高光/阴影)。
- `gui/interfaces/swing/EncryptDogFrame.java`: 进入执行页关闭内窗毛玻璃、返回表单页恢复;home 图标位于标题栏下方。
- 不涉及 core、终端模式与其他组件。
