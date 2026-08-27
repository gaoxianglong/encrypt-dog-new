## Why

执行页蒙层顶部当前与 home 返回图标仅有 2px 间隙,视觉上几乎挨着 logo,过于局促。用户要求蒙层顶部低一点点,给图标与蒙层之间留出轻微呼吸感。

## What Changes

- 执行页蒙层顶部微调:`WIDE_CARD_Y` 24 → 34,蒙层顶与图标底间隙 2px → 约 12px(比当前低 10px)。
- 蒙层底部保持距内容区底部 44px 不变,蒙层高 686 → 676,列表可视行数 ≥10 保持。
- 蒙层内内容、窗口标题栏、home 图标位置、蒙层无顶部高光带、表单页均不变。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 修改「执行表格视图」要求——蒙层顶部与返回图标之间保留约 12px 间隙(顶部约 y=34),不再紧贴图标。

## Impact

- `gui/interfaces/swing/EncryptDogFrame.java`: `WIDE_CARD_Y` 24 → 34(蒙层顶部微调)。
- `ProgressPanel.java` 无需改动:内容随卡片自动下移,内部相对布局不变。
- 不涉及 core、终端模式与其他组件。

## 前置依赖

本变更基于未归档变更 `gui-execution-overlay-layout` 与 `gui-execution-overlay-move-up`。归档顺序:`gui-execution-overlay-layout` → `gui-execution-overlay-move-up` → 本变更。
