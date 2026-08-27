## Context

执行页布局在近期多轮反馈中被改乱:统计小窗行被误删(以纯文字行替代),内窗透明效果被反复开关。用户最终确认的目标:恢复五个统计小窗(Operation/Files/Success/Failed/Elapsed,与"通过"时一致)+ 取消执行页内窗的透明(毛玻璃)打底,其余保持已确定状态。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 恢复五个统计小窗:高 54、横向等宽铺满、文字居中,begin/refresh/finish 驱动取值,操作类型显示 Encrypt/Decrypt。
- 执行页内窗取消透明打底(玻璃卡片不绘制半透明填充/描边/高光/阴影),表单页毛玻璃不变。
- 其余(表格 9 列/表头/进度条/结果图标/行排序/滚动/失败进度不强制 100%/完成停留/返回重置)一律不动。

**Non-Goals:**

- 不改表格与列表任何既有实现。
- 不改窗口标题栏、home 图标位置与表单页。

## Decisions

### D1: 恢复五个 StatCard(ProgressPanel)

恢复 StatCard 类(半透明白圆角小窗样式与"通过"时一致)与五个字段(statOperation/statFiles/statSuccess/statFailed/statElapsed),文字 horizontalAlignment CENTER;`begin` 填充操作类型与文件数、其余归零,`refresh` 实时累加成功/失败与耗时,`finish` 回填最终值;移除纯文字统计行(statsLabel);状态文字(statusLabel,算法信息/失败错误)回到内窗顶部小字。

布局:layoutContent 中 statusLabel (PADDING, 12, w-2P, 18);统计行 statY=36,卡宽 =(w-2×PADDING-4×CARD_GAP)/5,高 54;listY = 36+54+12;滚动区 h - listY - HEADER_H - 24。

### D2: 执行页内窗取消透明打底(GlassCardPanel + EncryptDogFrame)

GlassCardPanel 新增 `setFrosted(boolean)`:false 时 paintComponent 直接返回(不绘制阴影/半透明填充/描边/高光);EncryptDogFrame:startOperation 调用 `glassCard.setFrosted(false)`,backToForm 恢复 `true`。表格与统计小窗直接呈现于粒子背景之上。

## Risks / Trade-offs

- [无玻璃打底时,统计卡片/表格在粒子背景上的可读性] → 统计卡片自带半透明白填充与描边,表格行条/表头有自身底色,可读性由组件自身保证;验收时确认。
- [恢复改动范围] → 仅 D1/D2 两处,其余代码路径不触碰(用户明确要求不再乱改)。

## Migration Plan

纯 GUI 变更,无数据迁移;回滚即恢复纯文字统计行与毛玻璃打底,不影响功能行为与终端模式。
