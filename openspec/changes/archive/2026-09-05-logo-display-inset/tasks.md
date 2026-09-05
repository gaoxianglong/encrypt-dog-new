# 实现任务：托盘与标题栏 logo 恢复内置留白（82% 内框）

## 1. LogoUtil 内框渲染

- [x] 1.1 `LogoUtil` 新增私有常量 `LOGO_INSET = 0.82` 与私有重载 `loadImage(resource, size, insetFactor)`（缩放基准 `size × insetFactor`，管线其余不变）；公开 `loadImage(resource, size)` 以 1.0 委托，`loadLogo(size)` 以 `LOGO_INSET` 委托；缓存键改为 `resource@size@inset`。验证：`mvn compile` 通过。
- [x] 1.2 新增 `LogoUtil` 单测：`loadLogo(22/28/44)` 返回图标宽高 ≈ `round(size × 0.82)` 的等比缩放（alpha 包围盒宽度 = 内框宽度）、图形不变形；`loadImage` 同尺寸仍为顶满槽位语义（回归断言 1.0 委托）。验证：`mvn test` 相关用例通过。

## 2. 托盘切换渲染入口

- [x] 2.1 `TrayManager.install` 的 icon22/icon44 由 `loadImage(LOGO_RESOURCE, …)` 改为 `loadLogo(TRAY_ICON_SIZE/TRAY_ICON_SIZE_2X)`，双档配对与 `setImageAutoSize(false)` 不变。验证：`mvn compile` 通过；`--gui` 启动菜单栏图标出现。

## 3. 验收与回归

- [x] 3.1 jar 形态 `--gui` 目视验收：托盘图标内容不顶满槽位、视觉大小与相邻菜单栏原生图标相当；标题栏 logo 留出约 36% 视觉留白（64% 内框）、与 22pt 品牌文字平衡不偏大。验证：与 spec 场景「托盘图标视觉留白」「标题栏 logo 视觉留白」「标题栏 logo 与品牌名称协调」一致（用户验收通过）。
- [x] 3.2 回归：托盘空闲/干活五态菜单、tooltip、更新检查项、窗口隐藏与恢复、Quit 语义均正常；标题栏按钮、蒙层、主题渐变不受影响；终端模式（不带 `--gui`）执行一次加/解密行为不变（终端加解密回归已自动化验证通过：加密→解密→diff 一致；GUI 侧托盘交互用户点验通过）。
- [x] 3.3 微调预案已执行并定稿：全局 0.82 验收时托盘通过、标题栏偏大 → 拆两档常量（`LOGO_INSET=0.82` 托盘、`LOGO_TITLE_INSET=0.64` 标题栏）后标题栏验收通过，spec「约 82%」/「约 64%」两档表述已同步。
