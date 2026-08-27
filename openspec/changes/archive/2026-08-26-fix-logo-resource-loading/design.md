# Design: 修复标题栏 logo 资源加载

## Context

- 资源文件：`src/main/resources/logo.png` 已由用户替换为新 logo——1536×1024 RGBA、白色系图形（主色 rgb≈(224,224,224)）、alpha bbox (271,0,1404,976) ≈ 1133×976（宽高比 1.16:1）。
- `UiConstants.LOGO_RESOURCE` 磁盘当前值 `"file-empty.png"`（18:4x 误改），导致标题栏加载文件空态图标；`FILE_EMPTY_RESOURCE="file-empty.png"` 为独立常量，语义互不重叠。
- `LogoUtil.loadImage` 现实现：`image.getScaledInstance(size, size, SCALE_SMOOTH)`——强制方形缩放，3:2 画布被横向压扁。
- `TitleBar` 以 `LogoUtil.loadLogo(LOGO_TITLE_SIZE=28)` 加载，label 28×28，BoxLayout 垂直居中。

动机见 proposal.md - Why。

## Goals / Non-Goals

**Goals:**

- 标题栏加载用户提供的 logo.png
- logo 按原图比例显示，不变形
- 显示尺寸契约不变（`LOGO_TITLE_SIZE=28`，槽位 28×28）

**Non-Goals:**

- 不修改用户的 logo 资源文件（不预处理裁切）
- 不改标题栏结构与尺寸常量
- 不触碰终端模式与核心逻辑

## Decisions

### D1. `LOGO_RESOURCE` 恢复为 `"logo.png"`

- 唯一合法值；`LOGO_RESOURCE` 与 `FILE_EMPTY_RESOURCE` 各自独立，恢复不产生副作用。

### D2. `LogoUtil.loadImage` 改为等比缩放适配

- 运行时先按 alpha 包围盒裁掉透明边（`alphaBounds`，全透明返回 null 则原图直用），再计算 `scale = min(size/w, size/h)` 缩放至 `tw×th`（tw、th ≤ size），缩放用 Graphics2D.drawImage 同步绘制（规避 `getScaledInstance` 异步缩放未完成导致的残缺渲染）。
- JLabel 无文本纯图标默认水平/垂直 CENTER 对齐，图标小于槽位时居中显示、不裁切；12.9 修复针对的是"图标大于标签被裁切"，与本方案（图标 ≤ 槽位）不冲突。
- 新图内容 1133×976 裁边后等比适配 28 槽位 → 显示约 28×24；若不裁边直接按 1536×1024 画布缩放，内容仅显示约 21×18，透明边距挤压图形（apply 阶段验证发现，故补裁边步骤）。
- 备选：资源预处理（PIL 按 bbox 裁透明边 + 补白成正方形）→ 需改写用户的资源文件且无法目视验证裁切边界，拒绝。

### D3. 验证方案

- 无屏渲染（未显示 frame 的 addNotify+validate 全新渲染，规避陈旧双缓冲与随机粒子干扰）：
  - 标题栏 logo 区（14,9,28,28）出现新图签名（近白高 alpha 像素，rgb≈(224,224,224)），与旧 logo、file-empty 图标签名均不同；
  - 渲染图标 alpha bbox 宽高比 ≈ 1.16（与原图内容比例一致）→ 证明未变形。

## Risks / Trade-offs

- [等比缩放后图标视觉略小于槽位（28×24）] → 原图自带边距所致；用户如需更大可提供方形资源，常量不动。
- [白色系 logo 在深色标题栏上的对比] → 标题栏底色 0x10102A 深蓝紫，白色图形对比良好。

## Migration Plan

纯增量，无数据/格式变化；回滚 = revert 本 change 提交。

## Open Questions

（无）
