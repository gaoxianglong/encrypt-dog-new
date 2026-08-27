## Why

执行页顶部仍有留白:home 返回图标已紧贴窗口标题栏底部(2px 间隙),但蒙层顶距图标底仍有 48px 空隙,统计卡片与表格随之整体偏低。用户要求透明蒙层与其上的内容也往上移动一些。

## What Changes

- 执行页蒙层顶部上移:`WIDE_CARD_Y` 70 → 24,蒙层顶紧贴 home 图标底(2px 间隙),与「标题栏↔图标」的 2px 间距语言保持一致。
- 蒙层内内容(状态文字、统计小窗、表头、文件列表)随蒙层整体上移,内部相对布局不变;蒙层底部保持距内容区底部 44px(位于版权信息上方),蒙层高度相应增加约 46px,列表可视行数 ≥10 保持。
- 窗口标题栏各页常显、home 图标位置、蒙层无顶部高光带、表单页布局均不变。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 修改「执行表格视图」要求——蒙层顶部上移至 home 图标下方(顶部约 y=24),蒙层与其上内容整体上移,减少顶部留白。

## Impact

- `gui/interfaces/swing/EncryptDogFrame.java`: `WIDE_CARD_Y` 70 → 24(蒙层顶部上移)。
- `ProgressPanel.java` 无需改动:内容随卡片自动上移,内部相对布局不变。
- 不涉及 core、终端模式与其他组件。

## 前置依赖

本变更基于未归档变更 `gui-execution-overlay-layout`(窗口标题栏各页常显、蒙层无顶部高光带、home 图标紧贴标题栏底部)。归档顺序:先归档 `gui-execution-overlay-layout`,再归档本变更。
