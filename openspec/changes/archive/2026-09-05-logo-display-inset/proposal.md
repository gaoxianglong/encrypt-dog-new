# 托盘与标题栏 logo 视觉缩小（恢复内置留白）

## Why

用户验收反馈：菜单栏托盘图标与窗口标题栏 logo 观感偏大。根因：`LogoUtil.loadImage` 先按 alpha 包围盒裁掉 logo.png 原图自带的约 18% 透明边距、再把图形顶满槽位缩放，导致图形零留白；而 macOS 原生菜单栏图标惯例与项目 Dock 路径（82% 内框，见 dock-icon-rounded-corners）均保留约 18% 视觉留白，对比之下 logo 显得"胖"。槽位尺寸本身是既有排版契约（托盘 22pt/44pt@2x、标题栏 28px 与 22pt 品牌文字视觉同高，见 refine-title-and-logo-sizes），不宜回退——应恢复留白而非缩槽位。

## What Changes

- `loadLogo` 应用 82% 内框因子（与 Dock 圆角路径同一约定）：logo 内容在显示槽位内只占 82%，视觉缩小约 18%。
- 托盘图标改用 `loadLogo`（当前直接调通用 `loadImage`），1x/2x 槽位 22pt/44px 不变，内容视觉约 18pt，落在 Apple HIG 菜单栏图标推荐区间。
- 新增 `loadTitleLogo`：标题栏档位以 64% 内框渲染（图形约 18px，验收迭代定稿），`TitleBar` 改调该方法，槽位 28px 与布局不变。
- 槽位常量、缓存机制、其它图标资源（Dock、返回箭头、状态图标等）均不变；`loadImage` 通用"顶满槽位"语义不变。

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `swing-gui`: 「标题栏 logo 资源」需求扩展——标题栏 logo 内容 SHALL 以约 64% 内框渲染（槽位 28px 不变），图形约 18px 与 22pt 品牌文字视觉平衡；「标题栏 logo 与品牌名称协调」补上界——既不显过小也不显过大。
- `menu-bar-tray`: 「单进程托盘挂载与降级」需求扩展——菜单栏图标内容 SHALL 以约 82% 内框渲染（22pt 与 44pt@2x 双档槽位不变），视觉大小与 macOS 菜单栏原生图标留白惯例一致。

## Impact

- 受影响代码：`LogoUtil`（`loadLogo` 应用内框因子，新增 `LOGO_INSET` 常量或等价实现）、`TrayManager.install`（改调 `loadLogo`）；`TitleBar` 经 `loadLogo` 自动生效无需改动。
- 缓存：`ICON_CACHE` 键需区分带内边距与不带内边距的渲染变体，避免同尺寸旧缓存污染新渲染。
- 无 API/依赖变化；终端模式零影响；Dock 路径已含 82% 内框，不受影响。
