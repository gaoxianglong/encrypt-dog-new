## Context

执行页进度条现为 `ProgressPanel.GradientBar`(ACCENT→ACCENT_BRIGHT 双色渐变、半透明白轨道、条高 10),位于 9 列表格 Progress 列(宽比 215/1066),右侧紧贴百分比文本。用户提供参考实现 `/Users/johngao/Desktop/test-swing/EffortUI.java`(纯 Swing 自绘、无外部依赖):四色渐变马赛克进度条 + 每格确定性相位明灭 + 圆角深色轨道 + 填充动画;窗口与卡片尺寸由 `UiConstants` 常量驱动(表单卡片自动居中、执行页为宽卡片模式,随窗口放大自适应)。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 把 Effort 进度条渲染效果移植为执行页每个文件行的进度条组件,填充由真实进度驱动。
- 窗口放大以承载更厚更大的进度条,视觉冲击力提升。
- 既有行为不变:右侧百分比文本、失败行停留失败时刻进度、未产生进度显示 0%、排序/配色逻辑。

**Non-Goals:**

- 不移植演示代码的场景装饰(标题文字、像素小宠物、胶囊按钮、Faster/Smarter 标签、轨道缩短 1/3 构图)——它们不属于执行页。
- 不改表格其他列、统计小窗、蒙层结构、动画参数与终端模式。

## Decisions

### D1: 移植为 MosaicBar 组件(ProgressPanel 内部类)

以 EffortUI 的渲染逻辑新建 `ProgressPanel.MosaicBar` 替代 `GradientBar`:

- 四色渐变:乳白 #F2EFE9 → 灰 #9A9A9A → 淡紫 #B8A0E8 → 紫 #7A5FA8,采样 8 级色阶(照搬 `fourColorRamp`,淡紫/紫与新主色系协调);轨道 `TRACK_BASE #333333` 圆角深色;组件固定 253px 宽 × 12px 高、自左端起(照搬示例 `TRACK_END − SLIDER_X = 253`、`SLIDER_H = 12` 固定数值,不随列宽缩放),百分比标签紧贴条右端;马赛克绘制 SHALL 裁剪于圆角轨道形状内(照搬示例 `g.setClip(track)`),填充区呈现圆角而非直角。
- 马赛克:方格边长 = 条高/2(条高 14 → 7px 格、2 行),1px 缝隙;每格独立相位 `hash(col,row)*2π` 叠加共享 phase 做明灭,确定性无闪烁。
- 填充:fillEnd = 进度百分比 × 轨道宽,仅绘制填充区内方格(0% 无填充,与既有"未产生进度显示 0%"行为一致);进度由 `parsePercent(data.progress)` 解析,不再有演示代码的循环填充。
- 常量入 `UiConstants`(MOSAIC_* 四色与轨道色),与既有常量中枢结构一致。

### D2: 脉冲动画(共享 Timer)

ProgressPanel 新增单个 `javax.swing.Timer`(33ms):每帧推进共享 `phase` 并仅重绘行区(rowsPanel),所有行的 MosaicBar 从共享 phase 读值绘制。生命周期:`begin()` 启动、`removeNotify()`(返回表单/窗口关闭时面板脱离卡片)停止,避免表单页空转;完成态停留执行页时保持动画(与操作是否结束无关)。备选:每行各持 Timer → 放弃,行数多时资源浪费;共享单 Timer 最小。

### D3: 窗口与表格放大

`EncryptDogFrame`:执行页宽窗口 WIDE_WIDTH 1200 → 1400、WIDE_HEIGHT 800 → 880;ProgressPanel:ROW_H 40 → 46、进度列宽比 215 → 300(总和 1066 → 1151)、条高 10 → 12(与示例 SLIDER_H=12 一致,方格=条高/2=6px,与示例 cell=6 完全一致);主页(表单页)窗口/卡片尺寸保持原样(用户确认)。表单卡片 CARD_X/CARD_Y 居中公式与执行页宽卡片(随窗口自适应)无需改动,其余布局按宽度比例自动重排。百分比标签占宽 50 不变。

## Risks / Trade-offs

- [30fps 行区重绘性能] → 仅重绘 rowsPanel 且格子数量级为每行 ~40 格、可见 ~10 行,绘制量小;如实测卡顿再降 Timer 频率或裁剪重绘矩形。
- [窗口放大后固定坐标元素] → 版权文字、蒙层顶部 y 等为顶部/底部锚定,随常量自适应;验收时核对表单页与执行页无溢出/错位。
- [四色渐变与主色系协调性] → 淡紫 #B8A0E8 与高亮 #B49CF4 相近、紫 #7A5FA8 为深紫,与星空背景同色相;验收实测整体观感。

## Migration Plan

纯 GUI 变更,无数据迁移;回滚即恢复 GradientBar、原窗口/行高/列宽比常量,不影响功能与终端模式。
