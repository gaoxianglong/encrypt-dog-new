# Design: 纯紫色渐变背景(电路方案验收转向)

## Context

动机与转向见 proposal.md - Why。现状:仓库中已有 `CircuitPanel`(静态线路层+脉冲层+鼠标提亮+启动级联,Timer 60fps)、`CircuitLayout`(两套线路布局)、`UiConstants` 中 12 个 CIRCUIT_* 常量;`EncryptDogFrame` 以 `circuitPanel` 字段引用并持有 `stop()` 调用与 `forwardCardMouseToCircuit` 鼠标转发。验收裁定:背景动画渲染全部取消,仅保留紫色渐变。

## Goals / Non-Goals

**Goals:**

- 背景 = 纯紫色渐变(BG_TOP→BG_BOTTOM,与标题栏无缝衔接),零动画、零交互、零逐帧重绘
- 标题栏、蒙层、表单/执行页布局与配色不动

**Non-Goals:**

- 不保留任何电路/星空元素;不改 BG_TOP/BG_BOTTOM 色值;不动标题栏绘制代码(标题栏破坏问题以动画层移除后的复验为准)

## Decisions

### D1: 新建 `GradientBackgroundPanel`,删除 `CircuitPanel`/`CircuitLayout`

背景收敛为静态渐变后,电路语义类(布局、脉冲、提亮)全部为死代码,直接删除;新面板仅做渐变预渲染缓存(尺寸变化时重建),无 Timer、无鼠标监听。`FRAME_DELAY_MS` 保留——`DropFilePanel`(呼吸/涟漪)、`FieldHint`、`GradientButton` 仍在用。

- 备选:CircuitPanel 内留模式开关 → 否决,保留死代码无意义

### D2: 移除 Frame 的动画面板配套接线

`EncryptDogFrame` 中 `stop()` 调用与 `forwardCardMouseToCircuit` 鼠标转发随交互层一并移除(无交互面板,转发失去目标);`dispose()` 恢复为直接 `super.dispose()`。

- 备选:保留空转发 → 否决,死代码

### D3: 标题栏破坏问题的处理策略

最新 jar 截图复验标题栏未见异常、运行日志无异常,未能复现破坏现象;静态化移除 60fps 重绘层后,唯一可能的动态干扰源消失。交付后请用户复验标题栏(深蓝底、logo 与 EncryptDog 文字完整)。

## Risks / Trade-offs

- **[R1] 标题栏破坏未复现,静态化可能未根治** → 交付后用户复验;若仍破坏,需在纯渐变版本上进一步排查(渐变起点/标题栏绘制路径),必要时要求提供触发步骤
- **[R2] 丢失背景交互感** → 用户已明确裁定取消,接受;未来如需要可另起 change
- **[R3] 删除面板后遗留引用** → 全仓 grep 验证 `CircuitPanel`/`CircuitLayout`/`CIRCUIT_` 无残留
