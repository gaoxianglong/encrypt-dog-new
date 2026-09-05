# 设计：托盘与标题栏 logo 恢复内置留白（82% 内框）

## Context

动机见 proposal.md。现状约束：`LogoUtil.loadImage(resource, size)` 的管线是 alpha 包围盒裁切 → 顶满槽位等比缩放（`scale = min(size/w, size/h)`），供托盘（22/44 双档）与标题栏（经 `loadLogo`，28px）两处使用；该裁切是 fix-logo-resource-loading 引入的缺陷修复，不可回退。项目 Dock 路径 `loadRoundedImage` 已验证 82% 内框约定（内容收进 82%、留 18% 视觉留白，验收通过）。槽位尺寸是既有排版契约（refine-title-and-logo-sizes：标题栏 28px 与 22pt 品牌文字视觉同高；托盘 22pt/44pt@2x 多分辨率），本变更不缩槽位。

## Goals / Non-Goals

**Goals:**

- 托盘与标题栏 logo 内容按各自位置因子收进内框（托盘约 82%、标题栏约 64%），槽位与布局零变化
- 两处走同一条 logo 渲染管线，内框因子按位置两档、各自可调

**Non-Goals:**

- 不改 `loadRoundedImage`（Dock 路径已验收，动它无收益有回归风险；两条路径各自持有 0.82 约定）
- 不改 `logo.png` 原图、不改槽位常量、不缩 `loadImage` 的通用"顶满槽位"语义
- 不做托盘单色 template 图标（AWT 对 template 支持不干净，另立变更再说）

## Decisions

### D1：内框因子放 `LogoUtil`，按位置两档：`LOGO_INSET`（托盘）+ `LOGO_TITLE_INSET`（标题栏）

新增私有常量 `LOGO_INSET = 0.82`、`LOGO_TITLE_INSET = 0.64` 与私有重载 `loadImage(resource, size, insetFactor)`：缩放基准由 `size` 变为 `size × insetFactor`，其余管线（包围盒裁切、bicubic 同步绘制）不变。公开的 `loadImage(resource, size)` 以 `insetFactor = 1.0` 委托；`loadLogo(size)` 以 `LOGO_INSET` 委托（托盘档位）；新增 `loadTitleLogo(size)` 以 `LOGO_TITLE_INSET` 委托（标题栏档位）。

备选：缩槽位常量（方向 A）→ 否决：违反既有排版契约且留白问题仍在；不裁切、直接用原图自带边距 → 否决：原图边距 82%×73% 非对称（内容包围盒 164×146），且裁切是既有缺陷修复不可回退；给 `loadImage` 加公开参数 → 否决：通用工具语义应稳定，logo 约定收敛进 logo 专用入口；**两位置共用单常量 → 被验收证伪**：0.82 全局因子下托盘通过、标题栏仍偏大（28px 槽位远大于 22pt 文字视觉高度），拆两档各自可调。

### D2：`TrayManager.install` 改调 `loadLogo`，`TitleBar` 改调 `loadTitleLogo`

`icon22/icon44` 由 `loadImage(LOGO_RESOURCE, TRAY_ICON_SIZE/TRAY_ICON_SIZE_2X)` 改为 `loadLogo(TRAY_ICON_SIZE/TRAY_ICON_SIZE_2X)`；`TitleBar` 的 logo 由 `loadLogo` 改为 `loadTitleLogo`。槽位 22/44 与 28、`BaseMultiResolutionImage` 双档配对、`setImageAutoSize(false)` 均不变，仅图形在槽位内的占比变化。

### D3：缓存键区分变体

`ICON_CACHE` 键由 `resource@size` 改为 `resource@size@inset`。当前无同资源同尺寸异 inset 的并存场景，但键上显式区分可防止未来通用路径与 logo 路径同尺寸缓存互串。缓存为进程内存态，无迁移问题。

### D4：视觉验收以目视为准，因子按位置定稿

验收迭代：全局 0.82 → 托盘通过、标题栏偏大 → 标题栏降至 0.64 验收通过。定稿值：托盘 0.82（内容约 18pt）、标题栏 0.64（内容约 18px）。后续若仍不满意，仅调对应位置常量，另一位置不受影响；规格中"约 82%"/"约 64%"的表述无需改动。

## Risks / Trade-offs

- [0.82 视觉仍不理想] → D4：单常量微调，验收定稿后回写，无结构改动。
- [非方形源图宽度受限] → 内容 164×146，按 min 轴（宽）顶到 82% 内框，高方向实际占比约 73%——与 Dock 路径同一行为，非新增问题。
- [托盘 18pt 内容在低分辨率下锯齿] → 源图 200px 经 bicubic 下采样，22/44 双档仍由系统选档，风险低；必要时调 0.82→0.85。
- [回归面] → 改动仅 logo 渲染一处入口与托盘一处调用；托盘五态菜单、tooltip、更新项、标题栏按钮均不触及。

## Migration Plan

无数据/持久化变更。jar 形态即时生效；.app 形态随下次重打包生效。回滚 = 还原 `loadLogo` 与 `TrayManager.install` 两处改动（缓存键改动随行回滚）。
