## Context

全部主题色集中于 `gui/interfaces/swing/constant/UiConstants.java`,组件统一引用常量:ACCENT/ACCENT_BRIGHT 被 GradientButton(按钮渐变)、ProgressPanel(GradientBar 渐变、执行中状态文字)、SegmentedToggle(选中段)、DropFilePanel(选中行淡紫底、悬停脉冲插值 ACCENT↔ACCENT_BRIGHT)、EncryptDogGui(滚动条滑块/悬停/按下)、EncryptFormPanel(按钮前景)、ThemedConfirmDialog(高亮)引用;星空效果由 BG_TOP→BG_BOTTOM 背景渐变(ParticlePanel、ThemedConfirmDialog 绘制)、PARTICLE_DIM↔PARTICLE_BRIGHT 64 级粒子调色板、LINK_COLOR 星座连线构成。另有 FieldHint(BACKING #2B1B5C α170)与 ProgressPanel(RowStrip #2B1B5C α150)两处硬编码深紫底。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 主色 #AF97E5、高亮 #B49CF4 落地为 ACCENT/ACCENT_BRIGHT,全部引用组件随之整体换色。
- 星空粒子/连线/背景渐变向新主色系同色相派生,明暗层次关系不变。
- 两处硬编码深紫底随主色系重新派生并收敛为共享常量。

**Non-Goals:**

- 不改语义色(ERROR_RED/SUCCESS_GREEN)、文本色(TEXT_PRIMARY/TEXT_SECONDARY)、卡片半透明白(CARD_FILL/CARD_BORDER/CARD_SHADOW)、标题栏背景(TITLE_BAR_BG)与 BG_TOP(近黑)。
- 不改组件结构、动画参数与任何布局。

## Decisions

### D1: UiConstants 色值整体更新

锚点(用户给定):ACCENT #6C63FF → #AF97E5(175,151,229);ACCENT_BRIGHT #8E7BFF → #B49CF4(180,156,244)。

派生色(保持既有明暗/饱和度层次,色相由蓝紫 ~244° 平移至新紫 ~277°):

| 常量 | 现值 | 新值 | 说明 |
|---|---|---|---|
| PARTICLE_BRIGHT | #BDC9FF | #D5C7F3 | 粒子亮色:新紫浅色调 |
| PARTICLE_DIM | #5A6BD6 | #8A76C4 | 粒子暗色:新紫深色调 |
| LINK_COLOR | #6C7BFF | #9D89DC | 星座连线:亮暗之间 |
| BG_BOTTOM | #23104A | #2B1A52 | 背景渐变底部深紫 |
| BG_TOP | #0B0B1E | 不变 | 近黑 |
| BUTTON_DISABLED | #4A4A6E | #5B5478 | 禁用态灰紫随色相平移 |
| FieldHint.HINT_FOREGROUND | #C0B8FF | #CBBDF1 | 提示文字:浅于高亮色的亮薰衣草紫 |
| DEEP_ACCENT(新增) | —(源自 #2B1B5C) | #3A2A66 | 深紫底共享常量 |

备选:逐组件硬编码新色值 → 放弃,破坏现有"常量中枢"结构且改动面大;常量集中更新一处生效。

### D2: 硬编码深紫底收敛为共享常量

UiConstants 新增 `DEEP_ACCENT = new Color(0x3A2A66)`;FieldHint.BACKING 改为 `new Color(DEEP_ACCENT.getRed(), ..., 170)` 语义(保留各自 alpha 170/150);ProgressPanel RowStrip 填充同源。两处原本同 RGB 不同 alpha,收敛后派生一致性有保证。

## Risks / Trade-offs

- [主色变浅后白字按钮文本对比度下降] → 新 ACCENT 亮度高于旧值,白色按钮文本对比度降低;验收时实测可读性,如不可接受再讨论(加深主色或改深色按钮文本,均属小幅调整)。
- [派生色值的观感预期] → 派生表为按色相平移的推导值,用户未给定具体星空色值;验收实测星空效果,不满意可仅调派生常量。
- [改动范围回归] → 仅 UiConstants 色值 + 两处硬编码替换,无结构改动,其他页面零触碰。

## Migration Plan

纯色值变更,无数据迁移;回滚即恢复 UiConstants 原色值与两处硬编码,不影响功能与终端模式。
