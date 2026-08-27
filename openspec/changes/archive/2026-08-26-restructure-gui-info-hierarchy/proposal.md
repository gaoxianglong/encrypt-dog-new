# Restructure GUI Info Hierarchy

## Why

当前界面品牌信息重复出现（窗口标题栏与内容卡片头部各有一组 logo + "EncryptionDog"），版本号放在卡片头部抢占视觉焦点，内容区缺少"当前任务是什么"的语境说明。需重构为"应用品牌—当前任务—具体操作"三级信息层级，让界面意图更清晰。

## What Changes

- 窗口标题栏仅保留一组 logo + "EncryptionDog" 作为应用品牌标识；logo 尺寸 22→24，与标题文字同一水平线。
- 内容卡片头部移除重复品牌（logo/标题/版本号），改为任务标题 "Encrypt files"（主标题）与副标题 "Protect your files with local encryption"（功能说明）。
- Encrypt/Decrypt 等宽分段选择器紧接任务标题，保持为模式切换控件（几何位置不变，紧随标题块）。
- 版本号移至内容卡片右下角（小字号次要色），避免抢占视觉焦点。
- 三级层级：标题栏=应用品牌 → 卡片头部=当前任务 → 表单控件=具体操作。

## Capabilities

### New Capabilities

（无）

### Modified Capabilities

- `swing-gui`: 新增一条需求「信息层级」，约束品牌单一性、任务标题/副标题、模式切换位置与版本号位置。

## Impact

- **代码**：`EncryptFormPanel`（卡片头部重排：移除 logo/标题/版本号，新增任务标题与副标题，版本号移至右下角）；`UiConstants`（LOGO_TITLE_SIZE 22→24）；`TitleBar` 无结构改动（BoxLayout 已保证同线居中）。
- **依赖**：无新增。
- **回归面**：卡片头部以下所有表单几何不变（任务标题块与旧头部同高预算）；终端模式不受影响。
