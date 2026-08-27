## Context

当前执行阶段:`startOperation` 切 ProgressPanel(逐文件进度条+耗时)→ `finishOperation` 切 ResultPanel(摘要+爆发动画/抖动)→ "再来一次" 回表单。窗口恒为 760×760 固定方形。用户要求:执行时窗口拉伸为 16:10 长方形、执行页改为 8 列实时表格+底部汇总、完成后停留执行页(保留音效)加 Back 返回。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 窗口在表单页保持 760×760,进入执行页平滑拉伸至 1120×700(约 1.1s),Back 反向缩回。
- 执行页为实时表格(8 列)+ 底部汇总 + Back 按钮;完成后停留并回填最终数据,播放完成提示音。
- 删除 ResultPanel、粒子爆发与失败抖动路径;失败也停留表格页。
- 终端模式不变。

**Non-Goals:**

- 不改表单页任何布局与坐标。
- 不做表格排序/筛选/导出。
- 不改加解密核心与终端 Dashboard。

## Decisions

### D1: 窗口形态直接切换(EncryptDogFrame)

`switchWindowShape(boolean toWide)`:一次性 `setSize`(760×760 ↔ 1200×800)+ 按原屏幕中心重算 `setLocation` + `setShape` 圆角 + `relayoutContent` 重排,不播放逐帧动画。验收迭代结论:macOS 上逐帧 resize(无论降频/移出表格/去 revalidate)仍卡顿,用户确认改为直接切换。宽屏卡片 bounds=(20, 54, w-40, 内容高-98),底边保持在版权信息上方。startOperation:switchWindowShape(true) + 内容一次性直接切换(移除 fadeCard 及其逐帧重绘,验收反馈渲染卡顿);backToForm:直接换回表单 + switchWindowShape(false)。TitleBar 移除背景色填充(TITLE_BAR_BG),仅保留品牌文字/logo/窗口按钮与拖拽(标题栏样式取消)。home 返回图标 20px。

### D2: 三层布局 + 9 列自绘列表(重写 ProgressPanel)

JTable 多轮样式调整仍与主题不搭(用户验收反馈),最终弃用 JTable,改为**自绘行列表**:行数据快照 `List<RowData>` + 行容器 null 布局,每行 = 半透明圆角行条(白 α10、圆角 12、无网格)+ 9 列 JLabel;表头为主题色小号粗体文字行;列宽按比例(36/230/85/85/75/140/85/230/100)随面板宽度等比缩放;路径列左对齐省略号+tooltip,其余列居中;滚动由 JScrollPane 承担(滚动条沿用 FlatLaf 主题色)。三层布局:

1. 第一层:左上角返回图标按钮(home-fill.png 26px,替代文字 Back 按钮,点击=backToForm)+ 操作标题("Processing…"→"Operation completed"/"Operation failed"),始终 TEXT_PRIMARY 白色,在标题容器内垂直居中、左侧间距保持(PADDING+34),字号 TASK_TITLE_FONT_SIZE 与首页一致;
2. 第二层:四个统计小窗 StatCard(高度紧凑为 58、横向等宽铺满),数值 20px/名称 11px;

滚动条:FlatLaf 真正读取 `ScrollBar.thumbColor` 键(此前仅设置了 Metal 时代的 thumb/thumbHighlight 键,颜色未生效),已在 EncryptDogGui 补上,执行列表滚动条即主题紫。**滚动生效修复**:rowsPanel 为 null 布局、首选尺寸缺省 0,ViewportLayout 视内容未超出视口故从不滚动——rebuildRows 时显式 `setPreferredSize` 为行总高度。表头:HeaderBand 半透明深紫圆角条(0x2B1B5C α150)作为背景,不再全透明。

行排序:refresh 后按状态优先级稳定排序(进行中 0 > 等待中 1 > 已完成 2),序号列随排序重排。

列表高度:窗口加高至 1200×800,标题/统计区压缩(统计窗高 54),滚动区高度 ≈ 卡高-186 ≈ 470,约容纳 10 行(行高 46);水平滚动条策略 NEVER(列宽随面板等比缩放,无水平溢出)。
3. 第三层 9 列自绘列表。

进度条:纯紫色渐变(ACCENT→ACCENT_BRIGHT)填充 + 半透明白轨道,无颗粒装饰(用户验收反馈,移除粒子/泛光/计时器)。

**行数据生命周期**:begin 先 `rows.clear()` 再填充(修复重复行 bug——此前 begin 未清空,二次操作时新行叠加旧行);refresh 由快照重建行;finish 按 sourceFile 合并最终结果;布局由覆写 `setBounds` 触发 `layoutContent()`(isShowing 守卫)。

刷新:`refresh(OperationProgressDTO)` 更新 TableModel(行数=文件数,每秒一帧快照,O(n) 更新可接受);执行中目标文件列即有值(解析阶段已知),处理后大小显示 "-" 直至完成回填。

首屏:`begin(operation, algorithm, files)` 立即设置操作标题、底部汇总并占位全部文件行(waiting 状态),消除首个进度快照(约 1 秒)到达前的空白等待态。

### D3: 数据补充(进度 DTO + ACL)

`OperationProgressDTO.FileProgress` 增加 `targetFile`、`targetFileSize`;`EncryptCoreFacade` 提取层:目标文件由解析阶段数据填充(与 previewTargetFiles 同源),处理后大小与错误原因在文件完成时回填(执行快照合并自执行结果上下文)。失败行错误原因来源 `OperationResultDTO.FileResult.errorMsg`。

### D4: 完成停留与音效迁移

`finishOperation`:不再切面板——将 `OperationResultDTO` 合并进表格模型(回填 targetFileSize/errorMsg/最终结果、停止快照刷新),调用 `progressPanel.finish(result)` 播放完成提示音(音效实现迁移自 ResultPanel,apply 时定位其调用点);删除粒子爆发调用。`operationFailed`:同样停留表格页,异常信息写入表格与汇总,删除回表单+抖动路径(shakeCard 移除)。Back 按钮 → 现有 `backToForm`(含表单 reset 语义不变)。

### D5: 清理

删除 `ResultPanel` 类、`EncryptDogFrame` 中结果页切换/爆发/抖动相关方法与字段;`UiConstants` 中仅结果页使用的常量(如爆发/抖动参数)同步清理。

## Risks / Trade-offs

- [表格行数大时每帧全量模型更新] → 行数=文件数且快照 1 秒一帧,O(n) 更新成本低;实测卡顿再改为增量 diff。
- [窗口 resize 动画与粒子 60fps 并存掉帧] → 粒子面板随尺寸重定位、无额外绘制成本;动画仅约 1.1s。
- [音效调用点迁移遗漏] → apply 时先定位现音效实现(ResultPanel 内),迁移后实测确认仍出声。
- [宽屏下表格列拥挤] → 源路径/目标文件列弹性+省略号,固定列按最小内容宽度。

## Migration Plan

纯 GUI 变更,无数据迁移;回滚即恢复结果页与方形窗口,不影响已加密产物与终端模式。
