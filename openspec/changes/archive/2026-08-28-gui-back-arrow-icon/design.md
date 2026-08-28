## Context

执行页返回按钮由 `EncryptDogFrame` 的 homeButton(20×20、tooltip "Back to form")承载,图标经 `LogoUtil.loadImage(UiConstants.HOME_RESOURCE, 20)` 加载 `home-fill.png`(房子图标)。用户提供新返回箭头图标 `/Users/johngao/Desktop/result-success.png`(2048×2048 PNG),要求替换。动机见 proposal.md「Why」。

## Goals / Non-Goals

**Goals:**

- 返回按钮改用用户提供的左向返回箭头图标,位置/尺寸/tooltip/行为不变。

**Non-Goals:**

- 不改表单页、标题栏 logo 等其他图标与任何布局。

## Decisions

### D1: 资源引入(64×64 缩放副本)

用 macOS `sips` 将用户选定的图标文件缩放为 64×64 并复制为 `src/main/resources/back.png`(64px 足够 20px 按钮高清渲染);原 `home-fill.png` 无其他引用,随之删除。注:首轮选用 `/Users/johngao/Desktop/result-success.png` 未通过验收,最终以用户指定的 `/Users/johngao/Downloads/3.1 返回.png`(200×200)为准。

### D2: 常量与引用切换

`UiConstants`:删除 `HOME_RESOURCE`,新增 `BACK_RESOURCE = "back.png"`;`EncryptDogFrame` homeButton 加载改为 `LogoUtil.loadImage(UiConstants.BACK_RESOURCE, 20)`。字段名 homeButton 保留(改动最小),按钮语义不变。

## Risks / Trade-offs

- [20px 下箭头清晰度] → 64px 源由 LogoUtil 缩放到 20px 渲染,足够清晰;验收实测。
- [用户源文件为桌面路径,不入库] → 缩放副本入 `src/main/resources/`,构建产物自包含。

## Migration Plan

纯资源与常量切换,无数据迁移;回滚即恢复 `home-fill.png` 与 `HOME_RESOURCE`,不影响功能与终端模式。
