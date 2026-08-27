## Context

首页卡片当前结构（卡片约 760×610）：主标题 (7..41)、副标题 (41..57)、模式切换栏 y=64、文件拖放区 y=104、选择按钮 y=220、Secret key y=252、确认密钥行 y≈298、Algorithm 行 y=344（layoutRows 基准，目标目录/选项/主按钮 390/436/466/522 随之）、版本号右下角 y=578。GlassCardPanel 顶部 60px 高光描边带底边即用户所指「把蒙层分成 2 部分的分割线」。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 首页蒙层为干净统一的半透明面（无顶部高光带/分割线），与执行页蒙层一致。
- 取消主副标题，模式切换成为蒙层首行，表单内容整体上移，顶部无空洞。
- 解密模式确认密钥行隐藏与整体上移动逻辑不变；版本号右下角不动。

**Non-Goals:**

- 不改执行页布局、表单控件样式、终端模式。

## Decisions

### D1: 整体上移 50px（标题块高度 57−7=50）

固定行坐标统一 −50：modeToggle 64→14、dropPanel 104→54、选择按钮 220→170、secretKey 252→202、确认密钥 298/306→248/256；layoutRows 基准 352/344→302/294（其余行随之）；主按钮 522→472（底 514，仍高于版本号 578，无重叠）；versionLabel 578 不动（底部右下角锚定）。

- 备选 A:只删标题留出顶部空洞 → 与用户一贯减少留白的要求相悖,放弃。
- 备选 B:上移量按"标题块实际高度"逐像素测量 → 实现复杂度无收益,固定 50px 更简单。

### D2: 主副标题彻底移除

删除 taskTitleLabel/subtitleLabel 与 ENCRYPT_TITLE/SUBTITLE/DECRYPT_TITLE/SUBTITLE 常量,switchMode 不再更新标题文案,仅保留模式切换与确认密钥行联动。

### D3: GlassCardPanel 删除 topHighlight 开关

两页面均不再需要高光带:删除字段/Setter 与 paintComponent 中的高光绘制,EncryptDogFrame 移除 startOperation/backToForm 的 setTopHighlight 调用,蒙层常画干净半透明面。

## Risks / Trade-offs

- [模式辨识度下降(标题被移除)] → 模式切换分段选择器本身已明确展示 Encrypt/Decrypt,操作类型执行页另有 Operation 统计小窗,辨识度可接受。
- [内容上移与版本号重叠] → 主按钮底 514 < 版本号顶 578,无重叠。

## Migration Plan

纯 GUI 变更,无数据迁移;回滚即恢复标签、高光与旧坐标。
