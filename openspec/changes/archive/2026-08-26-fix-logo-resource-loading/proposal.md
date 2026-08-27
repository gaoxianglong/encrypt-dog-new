# Proposal: 修复标题栏 logo 资源加载

## Why

用户已将新 logo（1536×1024 RGBA，白色系图形）放置到 `src/main/resources/logo.png` 并自行重建了 jar，但标题栏加载的图标不对。根因有二：(1) 磁盘上 `UiConstants.LOGO_RESOURCE` 当前值为 `"file-empty.png"`（18:4x 的编辑把 logo 资源路径改成了文件空态图标，与用户诉求冲突，需恢复为 `"logo.png"`）；(2) 新图为 3:2 画布，`LogoUtil.loadImage` 强制按 `size×size` 方形缩放，横向压扁变形。需要恢复正确的资源路径，并让 logo 按原图比例显示。

## What Changes

- `UiConstants.LOGO_RESOURCE` 恢复为 `"logo.png"`（当前磁盘值为 `"file-empty.png"`，属误改）。
- `LogoUtil.loadImage` 由方形强制缩放改为等比缩放适配：保持原图宽高比、缩放后居中于显示区域内，不拉伸变形。
- 重建 jar 打包新 logo.png（1164070 字节）；标题栏 logo 显示尺寸常量 `LOGO_TITLE_SIZE=28` 不变。
- 文件空态图标（`FILE_EMPTY_RESOURCE`）独立，不受影响。

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `swing-gui`: 新增"标题栏 logo 资源"需求——标题栏加载应用 logo 资源且按原图比例显示的契约。

## Impact

- 仅改动 `gui/interfaces/swing/constant/UiConstants.java`（1 行常量值）与 `gui/interfaces/swing/LogoUtil.java`（缩放逻辑）。
- 无 API、依赖、数据格式变化；终端模式零影响。
