# Proposal: 标题字号与 logo 尺寸协调

## Why

当前卡片任务标题 "Encrypt files" 为 26pt 加粗，比标题栏品牌名称 "EncryptionDog"（22pt）还大，品牌—任务层级失衡、观感奇怪；标题栏 logo 仅 24px，相对 22pt 品牌文字显得过小。需要将任务标题字号降下来、标题栏 logo 尺寸提上去。

## What Changes

- 任务标题字号 `UiConstants.LOGO_FONT_SIZE` 26→22（与标题栏品牌名称同号，不再大于品牌）；标签 bounds 不变，头部块几何与居中不受影响。
- 标题栏 logo 尺寸 `UiConstants.LOGO_TITLE_SIZE` 24→28（46px 标题栏内上下留 9px，视觉高度与品牌文字相当）。
- 常量更名：`LOGO_FONT_SIZE` → `TASK_TITLE_FONT_SIZE`（该常量在头部重构后仅服务任务标题，原名已名不副实）。
- 字号、颜色、文案除上述外均不变；下方模块几何零变化。

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `swing-gui`: 新增"字号层级"需求——任务标题字号不高于品牌名称、标题栏 logo 与品牌名称视觉协调的排版契约。

## Impact

- 仅改动 `gui/interfaces/swing/constant/UiConstants.java` 两个常量（值/名）及 `EncryptFormPanel.java` 中的常量引用（`TitleBar.java` 通过常量引用自动生效，无需改动）。
- 无 API、依赖、数据格式变化；终端模式零影响。
