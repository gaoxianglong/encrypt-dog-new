# Design: GUI Info Hierarchy Restructure

## Context

当前 `EncryptFormPanel` 卡片头部为 logo(40×40,y12) + "EncryptionDog"(26pt,y16) + 版本号(y30)，与标题栏的 logo+名称构成品牌重复。头部高度预算 y12..64（52px），下方模块（分段选择器 y64 起）已精调。窗口标题栏：logo 22 + 标题 22 加粗，BoxLayout(X_AXIS) 垂直居中。

## Goals / Non-Goals

**Goals:**

- 品牌单一（仅标题栏）、内容区表达"当前任务"、操作区不变
- 头部重排后下方所有几何零变化（头部高度预算不变）

**Non-Goals:**

- 不改标题栏结构（仅 logo 尺寸）
- 不改表单控件与模块布局

## Decisions

### D1. 卡片头部改为任务标题 + 副标题（高度预算不变）

- 移除 logo 图标、品牌标题、旧版本号标签。
- 任务标题 "Encrypt files"：bounds（LABEL_X=30, 14, 400, 34）、26pt 加粗、`TEXT_PRIMARY`。
- 副标题 "Protect your files with local encryption"：bounds（30, 48, 420, 16）、13pt、`TEXT_SECONDARY`。
- 副标题底边 64 = 分段选择器顶边 64 —— "紧接任务标题"且下方模块几何（64..230 及以下全部）**零变化**。

### D2. 版本号移至卡片右下角

- bounds（CARD_WIDTH-150=430, 578, 134, 14）、11pt、`TEXT_SECONDARY`、右对齐。
- 与主按钮（522..566，居中 x160..420）无重叠；与错误提示行（498..520 全宽）垂直错开；视觉上退居角落。

### D3. 标题栏 logo 放大并与标题同线

- `LOGO_TITLE_SIZE` 22→24；标题字号保持 22 加粗。
- BoxLayout(X_AXIS) 已保证两者垂直居中同线（JLabel alignmentY=0.5），无结构改动。

## Risks / Trade-offs

- [头部重排挤压任务标题可读性] → 标题 34px 高 + 副标题 16px 高 = 52px 预算内，26pt 标题行高 ~34 ✓
- [版本号与按钮/错误提示视觉拥挤] → 右下角 y578 与按钮底 566 相隔 12px、与错误行 520 相隔 58px，无重叠
- [回归面] → 下方表单几何零变化（仅头部内容替换），apply 后按任务 2.1 截图验证对齐

## Migration Plan

纯增量，无数据/格式变化；回滚 = revert 本 change 提交。

## Open Questions

（无）
