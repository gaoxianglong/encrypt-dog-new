## Why

执行页返回按钮当前使用房子形状的 `home-fill.png` 图标,语义上更接近"首页";用户提供了新的左向返回箭头图标(`/Users/johngao/Desktop/result-success.png`,2048×2048 PNG),认为箭头更贴合"返回"语义。

## What Changes

- 执行页返回按钮图标 SHALL 由 `home-fill.png`(房子)替换为用户提供的左向返回箭头图标;按钮位置/尺寸(20×20)/tooltip("Back to form")/点击行为 SHALL 保持不变。
- 新图标 SHALL 缩放至 64×64 后以 `back.png` 打入资源目录;原 `home-fill.png` 与相关常量 SHALL 移除(无其他引用)。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 修改「执行表格视图」要求——返回图标改为左向返回箭头。

## Impact

- `src/main/resources/`: 新增 `back.png`(64×64),移除 `home-fill.png`。
- `gui/interfaces/swing/constant/UiConstants.java`: `HOME_RESOURCE` 替换为 `BACK_RESOURCE = "back.png"`。
- `gui/interfaces/swing/EncryptDogFrame.java`: homeButton 图标加载改用 `BACK_RESOURCE`。
- 不涉及 core、终端模式与其他组件。
