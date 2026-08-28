## Why

当前 GUI 主色系偏蓝紫(强调色 #6C63FF/#8E7BFF,星空粒子与连线也偏蓝),用户希望整体调整为更柔和统一的紫色系:主色 #AF97E5、高亮 #B49CF4。涉及全部主题强调界面(按钮、进度条、分段选择器、滚动条、选中态、拖拽提示)与星空粒子背景。

## What Changes

- 主题主色 ACCENT SHALL 调整为 #AF97E5,高亮色 ACCENT_BRIGHT SHALL 调整为 #B49CF4;所有引用 ACCENT/ACCENT_BRIGHT 的组件(主按钮渐变、进度条渐变、分段选择器选中段、滚动条滑块与悬停、文件列表选中态、拖拽悬停脉冲、执行中状态文字、提示文字等)随之整体换色,无需逐组件改动。
- 星空效果 SHALL 向新主色系色相派生:粒子亮/暗色与星座连线、背景渐变底部深紫调整为与主色协调的紫色调(具体色值见 design.md,保留既有明暗层次关系);背景渐变顶部近黑保持不变。
- 两处硬编码深紫底(#2B1B5C:FieldHint 提示背景、ProgressPanel 行条填充)SHALL 随主色系重新派生并收敛为共享常量。
- 语义色(错误红、成功绿)、主/次文本色、卡片半透明白等中性色 SHALL 保持不变。

## Capabilities

### New Capabilities

<!-- 无新增能力 -->

### Modified Capabilities

- `swing-gui`: 新增「主题主色系」要求——主色 #AF97E5、高亮 #B49CF4,主题强调组件与星空背景按新主色系渲染,语义色不变。

## Impact

- `gui/interfaces/swing/constant/UiConstants.java`: ACCENT、ACCENT_BRIGHT、PARTICLE_BRIGHT、PARTICLE_DIM、LINK_COLOR、BG_BOTTOM、BUTTON_DISABLED 色值调整;新增深紫共享常量。
- `gui/interfaces/swing/FieldHint.java`、`gui/interfaces/swing/ProgressPanel.java`: 两处硬编码 #2B1B5C 改用共享常量。
- 其余组件(GradientButton/ProgressPanel/SegmentedToggle/DropFilePanel/EncryptDogGui 滚动条/EncryptFormPanel/ThemedConfirmDialog/ParticlePanel)仅经常量引用自动生效,无逐处改动。
- 不涉及 core、终端模式。
