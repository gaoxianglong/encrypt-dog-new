# 纯紫色渐变背景(电路轨迹方案验收转向)

## Why

电路轨迹背景实现后验收不通过:背景动画渲染被否决(用户要求"取消所有的背景动画渲染,只保留紫色"),同时上报标题栏被破坏。最终方向收敛为:背景回归**纯紫色渐变**(BG_TOP 近黑深蓝 → BG_BOTTOM 深紫,与标题栏无缝衔接),**无任何动画、装饰元素与交互**——星空粒子与电路轨迹两个方案全部退役,CPU 开销归零。

## What Changes

- 删除 `CircuitPanel`(电路轨迹背景:静态线路层、数据脉冲、鼠标提亮、启动级联)与 `CircuitLayout`(两套线路布局)
- 新建 `GradientBackgroundPanel`:仅绘制紫色渐变(预渲染缓存,尺寸变化时重建),无 Timer、无动画、无鼠标交互
- `EncryptDogFrame`:引用替换为 `GradientBackgroundPanel`;移除 `stop()` 调用与鼠标转发(`forwardCardMouseToCircuit`)——无交互层后不再需要
- `UiConstants`:移除 12 个电路常量(CIRCUIT_*),`FRAME_DELAY_MS` 保留(拖拽呼吸/涟漪等界面组件仍在使用)
- 标题栏、蒙层、表单/执行页布局均不动;标题栏文字/logo 问题随动画层移除一并验证

不包含:加回星空或其他任何背景效果、改动 BG_TOP/BG_BOTTOM 色值。

## Capabilities

### New Capabilities

(无)

### Modified Capabilities

- `swing-gui`:
  - MODIFIED `主题主色系`:星空粒子背景着色语 → 纯紫色渐变背景语
  - MODIFIED `执行完成停留`:"不播放粒子爆发动画" → "不播放背景动画"(背景动画已整体取消,措辞同步)
  - REMOVED `星空粒子背景渲染性能`:动画背景整体退役,无替代性能 requirement(静态渐变无逐帧开销)

## Impact

- 代码:`gui/interfaces/swing/` 新建 `GradientBackgroundPanel.java`、删除 `CircuitPanel.java` 与 `CircuitLayout.java`;`EncryptDogFrame.java` 引用替换与转发移除;`UiConstants.java` 电路常量移除
- 用户可见:背景为纯紫色渐变(与标题栏无缝),无任何动画;其余界面不变
- 性能:空闲 CPU 由星空 78.7% / 电路 67.6% 降至接近 0(无逐帧重绘)
